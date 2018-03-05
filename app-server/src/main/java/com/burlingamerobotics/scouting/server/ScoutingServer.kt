package com.burlingamerobotics.scouting.server

import android.bluetooth.BluetoothServerSocket
import android.content.Context
import android.content.Intent
import android.util.Log
import com.burlingamerobotics.scouting.common.Utils
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.protocol.Event
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * Manages all the client threads
 */
object ScoutingServer {

    const val TAG = "ScoutingServer"

    val clients: MutableList<ClientResponseThread> = mutableListOf()

    lateinit var competition: Competition
    lateinit var serverSocket: BluetoothServerSocket
    lateinit var db: ScoutingDB

    private var serverListener: Future<*>? = null

    fun start(context: Context, db: ScoutingDB, serverSocket: BluetoothServerSocket, competition: Competition) {
        this.db = db
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

    fun broadcast(event: Event) {
        clients.forEach { it.sendEvent(event) }
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