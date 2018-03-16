package com.burlingamerobotics.scouting.server.io

import android.bluetooth.BluetoothServerSocket
import android.content.Context
import android.content.Intent
import android.util.Log
import com.burlingamerobotics.scouting.common.Utils
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.protocol.*
import com.burlingamerobotics.scouting.server.DURATION_SAVE_DATA
import com.burlingamerobotics.scouting.server.INTENT_CLIENT_CONNECTED
import java.io.Serializable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Manages all the client threads
 */
object ScoutingServer : ClientInputListener {
    const val TAG = "ScoutingServer"

    val clients: MutableList<ClientResponseThread> = mutableListOf()

    lateinit var competition: Competition
    lateinit var serverSocket: BluetoothServerSocket
    lateinit var db: ScoutingDB

    private var serverListener: Future<*>? = null

    fun start(context: Context, db: ScoutingDB, serverSocket: BluetoothServerSocket, competition: Competition) {
        ScoutingServer.db = db
        ScoutingServer.competition = competition
        ScoutingServer.serverSocket = serverSocket
        serverListener = Utils.ioExecutor.submit {
            while (true) {
                val client = ClientResponseThread(serverSocket.accept(), db)
                Log.i(TAG, "Bluetooth device at ${client.device.address} connected")
                clients.add(client)
                client.start()
                context.sendBroadcast(Intent(INTENT_CLIENT_CONNECTED))
            }
        }

        Utils.ioExecutor.scheduleWithFixedDelay({
            Log.d(TAG, "Saving data to disk")
            db.save(competition)
            db.commitTeams()
        }, DURATION_SAVE_DATA, DURATION_SAVE_DATA, TimeUnit.MILLISECONDS)

    }

    override fun onClientSentObject(client: ScoutingClient, obj: Any) {
        Log.d(TAG, "Received $obj from ${client.displayName}")
        when (obj) {
            is Request<*> -> {
                Log.d(TAG, "  It's a request")
                val response = Response(getItemByRequest(obj))
                Log.d(TAG, "Writing $response")
                client.sendObject(response)
            }
            is Post -> {
                Log.d(TAG, "  It's a post")
                processPost(client, obj)
            }
            else -> {
                Log.e(TAG, "  We don't know what to do with it!")
            }
        }
    }

    fun processPost(client: ScoutingClient, post: Post) {
        when (post) {
            is PostTeamInfo -> {
                Log.d(TAG, "  The post is a team change")
                db.putTeam(post.team)
                ScoutingServer.broadcast(EventTeamChange(post.team))
            }
            is PostChatMessage -> {
                Log.d(TAG, "  The post is a chat message")
                ScoutingServer.broadcast(EventChatMessage(client.displayName, post.message))
            }
            else -> {
                Log.e(TAG, "  We don't know how to react to $post!")
            }
        }
    }

    fun <T> getItemByRequest(request: Request<T>): Any? {
        return when (request) {
            is CompetitionRequest -> {
                Log.d(TAG, "  The request is for all competition data")
                ScoutingServer.competition
            }
            is QualifierMatchRequest -> {
                Log.d(TAG, "  The request is for .match info")
                ScoutingServer.competition.qualifiers!![request.number]
            }
            is TeamInfoRequest -> {
                Log.d(TAG, "  The request is for team info")
                db.getTeam(request.team)
            }
            is TeamListRequest -> {
                Log.d(TAG, "  The request is for a list of teams")
                db.listTeams()
            }
            else -> {
                Log.e(TAG, "  We don't know how to respond to $request!")
                null
            }
        }
    }

    fun broadcast(event: Event) {
        clients.forEach { it.sendObject(event) }
    }

    fun stop() {
        serverListener?.cancel(true)
        clients.forEach { it.close() }
        clients.clear()
    }

    fun onClientDisconnected(thread: ClientResponseThread) {
        clients.remove(thread)
    }

}