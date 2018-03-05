package com.burlingamerobotics.scouting.server

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.burlingamerobotics.scouting.common.protocol.*
import java.io.Closeable
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * The server's view of the client.
 */
class ClientResponseThread(private val btSocket: BluetoothSocket, private val db: ScoutingDB) : Thread(), Closeable {

    val TAG: String
    val device = btSocket.remoteDevice

    lateinit var ois: ObjectInputStream
    lateinit var oos: ObjectOutputStream

    init {
        name = "ClientInterfaceThread[${device.address}]"
        TAG = name
    }

    fun sendEvent(event: Event) {
        oos.writeObject(event)
    }

    override fun run() {
        Log.i(TAG, "Starting ObjectStreams")
        ois = ObjectInputStream(btSocket.inputStream)
        oos = ObjectOutputStream(btSocket.outputStream)

        try {
            Log.i(TAG, "Successfully started!")
            while (true) {

                val obj = ois.readObject()

                Log.d(TAG, "Received $obj")
                when (obj) {
                    is Request<*> -> {
                        Log.d(TAG, "  It's a request")
                        val response = Response(getItemByRequest(obj))
                        Log.d(TAG, "Writing $response")
                        oos.writeObject(response)
                    }
                    is Post -> {
                        Log.d(TAG, "  It's a post")
                        processPost(obj)
                    }
                    else -> {
                        Log.e(TAG, "  We don't know what to do with it!")
                    }
                }

            }
        } catch (ex: InterruptedException) {
            Log.i(TAG, "Interrupted, gracefully stopping thread")
        } catch (ex: IOException) {
            Log.w(TAG, "Client disconnected", ex)
        } finally {
            btSocket.close()
        }
        ScoutingServer.onClientDisconnected(this)
    }

    fun processPost(post: Post) {
        when (post) {
            is PostTeamInfo -> {
                Log.d(TAG, "  The post is a team change")
                db.putTeam(post.team)
                ScoutingServer.broadcast(EventTeamChange(post.team))
            }
            is PostChatMessage -> {
                Log.d(TAG, "  The post is a chat message")
                ScoutingServer.broadcast(EventChatMessage(device.name, post.message))
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

    override fun close() {
        interrupt()
    }

}