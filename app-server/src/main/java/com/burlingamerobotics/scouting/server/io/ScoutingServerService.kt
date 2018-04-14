package com.burlingamerobotics.scouting.server.io

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.content.Intent
import android.os.*
import android.util.Log
import com.burlingamerobotics.scouting.common.INTENT_BIND_LOCAL_CLIENT_TO_SERVER
import com.burlingamerobotics.scouting.common.INTENT_BIND_SERVER_WRAPPER
import com.burlingamerobotics.scouting.common.INTENT_CLIENT_CONNECTED
import com.burlingamerobotics.scouting.common.INTENT_CLIENT_DISCONNECTED
import com.burlingamerobotics.scouting.shared.DURATION_SAVE_DATA
import com.burlingamerobotics.scouting.shared.SCOUTING_UUID
import com.burlingamerobotics.scouting.shared.Utils
import com.burlingamerobotics.scouting.shared.data.Competition
import com.burlingamerobotics.scouting.shared.protocol.*
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit


class ScoutingServerService : Service(), Handler.Callback, ClientInputListener {

    /**
     * Handle requests from [ScoutingServerServiceWrapper].
     */
    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {

        }
        msg.replyTo
        return true
    }

    val TAG = "ScoutingServerService"

    private val clients = mutableListOf<ScoutingClientInterface>()
    private var btConnectListener: Future<*>? = null
    private var dataSaveTask: Future<*>? = null
    private val lockedResources = hashSetOf<MatchListResource>()

    var btAdapter: BluetoothAdapter? = null
    var serverSocket: BluetoothServerSocket? = null

    lateinit var competition: Competition
    lateinit var db: ScoutingDB

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "Received intent to bind: $intent")
        when (intent.action) {
            INTENT_BIND_LOCAL_CLIENT_TO_SERVER -> {
                Log.i(TAG, "Local client wants to bind")
                val newClient = LocalClientInterface()
                newClient.attachClientInputListener(this)
                clients.add(newClient)
                sendBroadcast(Intent(INTENT_CLIENT_CONNECTED).apply { putExtra("client", newClient.getInfo()) })
                return newClient.rx.binder
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
                    sendBroadcast(Intent(INTENT_CLIENT_CONNECTED).apply {
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

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Destroying")
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
                client.sendObject(Response(obj.uuid, processAction(obj)))
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
        Log.i(TAG, "$client disconnected, removing from list")
        clients.remove(client)
        sendBroadcast(Intent(INTENT_CLIENT_DISCONNECTED).apply { putExtra("client", client.getInfo()) })
    }

    fun processAction(action: Action): ActionResult {
        return when (action) {
            is EditTeamPerformanceAction -> {
                val resourceToLock = action.asResource()
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
                Log.d(TAG, "  The action is an intent to stop editing team performance")
                val tp = action.teamPerformance
                if (tp != null) {
                    Log.d(TAG, "It provides a TeamPerformance: $tp")
                    competition.qualifiers.matches[action.team].putTeamPerformance(tp)
                }
                lockedResources.remove(MatchListResource(action.match, action.team))
                ActionResult(true)
            }
        }
    }

    fun processPost(client: ScoutingClientInterface, post: Post) {
        when (post) {
            is PostTeamPerformance -> {
                Log.d(TAG, "  The post is a team performance change")
                competition.qualifiers.matches[post.team].putTeamPerformance(post.teamPerformance)
            }
            is PostTeamInfo -> {
                Log.d(TAG, "  The post is a team change")
                db.putTeam(post.team)
                broadcast(EventTeamChange(post.team))
            }
            is PostChatMessage -> {
                Log.d(TAG, "  The post is a chat message")
                broadcast(EventChatMessage(client.displayName, post.message))
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
                competition
            }
            is MatchRequest -> {
                Log.d(TAG, "  The request is for .match info")
                competition.qualifiers[request.number]
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

}

data class MatchListResource(val match: Int, val team: Int)

fun EditTeamPerformanceAction.asResource(): MatchListResource = MatchListResource(match, team)
