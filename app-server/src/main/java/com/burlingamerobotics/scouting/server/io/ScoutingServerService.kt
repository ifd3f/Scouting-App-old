package com.burlingamerobotics.scouting.server.io

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import com.burlingamerobotics.scouting.common.INTENT_BIND_LOCAL_CLIENT_TO_SERVER
import com.burlingamerobotics.scouting.server.INTENT_BIND_SERVER_WRAPPER
import com.burlingamerobotics.scouting.server.INTENT_SERVER_CLIENT_CONNECTED
import com.burlingamerobotics.scouting.server.INTENT_SERVER_CLIENT_DISCONNECTED
import com.burlingamerobotics.scouting.shared.DISCONNECT_REASON_SHUTDOWN
import com.burlingamerobotics.scouting.shared.DURATION_SAVE_DATA
import com.burlingamerobotics.scouting.shared.SCOUTING_UUID
import com.burlingamerobotics.scouting.shared.Utils
import com.burlingamerobotics.scouting.shared.data.Competition
import com.burlingamerobotics.scouting.shared.protocol.*
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit


class ScoutingServerService : Service(), Handler.Callback, ClientInputListener {

    private val TAG = "ScoutingServerService"

    private val clients = mutableListOf<ScoutingClientInterface>()
    private var btConnectListener: Future<*>? = null
    private var dataSaveTask: Future<*>? = null
    private val lockedResources = hashSetOf<MatchListResource>()

    private var btAdapter: BluetoothAdapter? = null
    private var serverSocket: BluetoothServerSocket? = null

    private lateinit var competition: Competition
    private lateinit var db: ScoutingDB

    /**
     * Handle requests from [ScoutingServerServiceWrapper].
     */
    override fun handleMessage(msg: Message): Boolean {
        Log.i(TAG, "Received from ServiceWrapper: $msg")
        when (msg.what) {
            SW_REQUEST_COMPETITION -> {
                Log.d(TAG, "It's a request for competition data, sending: $competition")
                val out = Message.obtain().apply {
                    what = SW_RESPONSE
                    data.putSerializable("result", competition)
                }
                msg.replyTo.send(out)
                Log.d(TAG, "Replying to: ${msg.replyTo} with $out")
            }
            SW_FORCE_DISCONNECT -> {
                val id = msg.data.getLong("sessid")
                val reason = msg.arg1
                Log.d(TAG, "It's a command to disconnect $id because $reason")
                val client = clients.find { it.id == id }
                if (client != null) {
                    forceDisconnect(client, reason)
                } else {
                    Log.e(TAG, "$id does not exist! Cannot disconnect someone who doesn't exist!")
                }
            }
            SW_PING -> {
                Log.d(TAG, "It's a ping! We'll send a pong!")
                msg.replyTo.send(Message.obtain().apply {
                    what = SW_PONG
                })
            }
        }
        return true
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "Received intent to bind: $intent")
        when (intent.action) {
            INTENT_BIND_LOCAL_CLIENT_TO_SERVER -> {
                Log.i(TAG, "Local client wants to bind")
                val newClient = LocalClientInterface()
                newClient.attachClientInputListener(this)
                clients.add(newClient)
                sendBroadcast(Intent(INTENT_SERVER_CLIENT_CONNECTED).apply { putExtra("client", newClient.getInfo()) })
                return newClient.binder
            }
            INTENT_BIND_SERVER_WRAPPER -> {
                Log.i(TAG, "Server manager wrapper wants to bind")
                return Messenger(Handler(this)).binder
            }
            else -> {
                Log.e(TAG, "Illegal intent action ${intent.action}")
                throw IllegalArgumentException("Invalid action: \"${intent.action}\"!")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Starting")
        btAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent!!
        db = ScoutingDB(this)
        competition = db.getCompetition(intent.getSerializableExtra("competition") as UUID)!!

        val adapter = btAdapter
        if (adapter != null) {
            Log.i(TAG, "Bluetooth adapter exists, starting bluetooth server socket")
            val sock = adapter.listenUsingRfcommWithServiceRecord("Scouting Server", SCOUTING_UUID)

            btConnectListener = Utils.ioExecutor.submit {
                while (true) {
                    val client = BluetoothClientInterface(sock.accept())
                    client.attachClientInputListener(this)
                    Log.i(TAG, "Bluetooth device at ${client.device.address} connected")
                    clients.add(client)
                    client.begin()
                    sendBroadcast(Intent(INTENT_SERVER_CLIENT_CONNECTED).apply {
                        putExtra("client", client.getInfo())
                    })
                }
            }

            this.serverSocket = sock
        } else {
            Log.w(TAG, "Bluetooth socket does not exist! Clients will not be able to remotely connect!")
        }

        dataSaveTask = Utils.ioExecutor.scheduleWithFixedDelay({
            Log.d(TAG, "Saving data to disk")
            db.save(competition)
            db.commitTeams()
        }, DURATION_SAVE_DATA, DURATION_SAVE_DATA, TimeUnit.MILLISECONDS)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Destroying")
        broadcast(EventForceDisconnect(DISCONNECT_REASON_SHUTDOWN))
        btConnectListener?.cancel(true)
        dataSaveTask?.cancel(true)
        clients.forEach { it.close() }
        clients.clear()
    }

    override fun onReceivedFromClient(client: ScoutingClientInterface, obj: Any) {
        Log.d(TAG, "Received $obj from ${client.displayName}")
        when (obj) {
            is Action -> {
                Log.d(TAG, "It's an action")
                client.sendObject(Response(obj.uuid, processAction(obj, client)))
            }
            is Request<*> -> {
                Log.d(TAG, "It's a request")
                val response = Response(obj.uuid, getItemByRequest(obj))
                Log.d(TAG, "Writing $response")
                client.sendObject(response)
            }
            is Post -> {
                Log.d(TAG, "It's a post")
                processPost(client, obj)
            }
            else -> {
                Log.e(TAG, "We don't know what to do with: $obj")
            }
        }
    }

    override fun onClientDisconnected(client: ScoutingClientInterface) {
        Log.i(TAG, "$client disconnected")
        clients.remove(client)
        lockedResources.removeAll { it.client == client }
        sendBroadcast(Intent(INTENT_SERVER_CLIENT_DISCONNECTED).apply { putExtra("client", client.getInfo()) })
    }

    private fun processAction(action: Action, client: ScoutingClientInterface): ActionResult {
        return when (action) {
            is EditTeamPerformanceAction -> {
                val resourceToLock = action.asResource(client)
                Log.d(TAG, "The action is an intent to edit team performance of $resourceToLock")
                if (lockedResources.contains(resourceToLock)) {
                    Log.w(TAG, "Already locked: $resourceToLock")
                    ActionResult(false)
                } else {
                    Log.d(TAG, "$resourceToLock is not locked")
                    val perf = competition.qualifiers[action.match - 1].getTeamPerformanceOf(action.team)
                    if (perf != null) {
                        Log.d(TAG, "Sending to client: $perf")
                        lockedResources.add(resourceToLock)
                        ActionResult(true, perf)
                    } else {
                        Log.d(TAG, "There is no TeamPerformance corresponding to $resourceToLock!")
                        ActionResult(false)
                    }
                }
            }
            is EndEditTeamPerformanceAction -> {
                Log.d(TAG, "The action is an intent to stop editing team performance")
                val tp = action.teamPerformance
                if (tp != null) {
                    Log.d(TAG, "It provides a TeamPerformance: $tp")
                    competition.qualifiers[action.match - 1].putTeamPerformance(tp)
                }
                lockedResources.remove(MatchListResource(client, action.match, action.team))
                ActionResult(true)
            }
            is DisconnectAction -> {
                Log.d(TAG, "The client wants to disconnect")
                ActionResult(true)
            }
        }
    }

    private fun processPost(client: ScoutingClientInterface, post: Post) {
        when (post) {
            /*
            is PostTeamPerformance -> {
                Log.d(TAG, "The post is a team performance change")
                competition.qualifiers.matches[post.team].putTeamPerformance(post.teamPerformance)
            }*/
            is PostTeamInfo -> {
                Log.d(TAG, "The post is a team change")
                db.putTeam(post.team)
                broadcast(EventTeamChange(post.team))
            }
            is PostChatMessage -> {
                Log.d(TAG, "The post is a chat message")
                broadcast(EventChatMessage(client.displayName, post.message))
            }
            else -> {
                Log.e(TAG, "We don't know how to react to $post!")
            }
        }
    }

    private fun <T> getItemByRequest(request: Request<T>): Any? {
        return when (request) {
            is CompetitionRequest -> {
                Log.d(TAG, "The request is for all competition data")
                competition
            }
            is MatchRequest -> {
                Log.d(TAG, "The request is for .match info")
                competition.qualifiers[request.number]
            }
            is TeamInfoRequest -> {
                Log.d(TAG, "The request is for team info")
                db.getTeam(request.team)
            }
            is TeamListRequest -> {
                Log.d(TAG, "The request is for a list of teams")
                db.listTeams()
            }
            else -> {
                Log.e(TAG, "We don't know how to respond to $request!")
                null
            }
        }
    }

    private fun broadcast(event: Event) {
        clients.forEach { it.sendObject(event) }
    }

    fun forceDisconnect(client: ScoutingClientInterface, reason: Int) {
        client.sendObject(EventForceDisconnect(reason))
    }

}

data class MatchListResource(val client: ScoutingClientInterface, val match: Int, val team: Int)

fun EditTeamPerformanceAction.asResource(client: ScoutingClientInterface): MatchListResource = MatchListResource(client, match, team)
