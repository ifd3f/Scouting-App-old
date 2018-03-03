package com.burlingamerobotics.scouting

import android.bluetooth.BluetoothServerSocket
import android.content.Context
import android.content.Intent
import android.os.Message
import android.util.Log
import com.burlingamerobotics.scouting.common.Utils
import com.burlingamerobotics.scouting.common.data.Competition
import java.util.concurrent.Future

/**
 * Manages all the client threads
 */
object ScoutingServer {

    const val TAG = "ScoutingServer"

    val clients: MutableList<ClientResponseThread> = mutableListOf()
    lateinit var competition: Competition
    lateinit var serverListener: Future<*>
    lateinit var serverSocket: BluetoothServerSocket

    fun start(context: Context, serverSocket: BluetoothServerSocket, competition: Competition) {
        this.competition = competition
        this.serverSocket = serverSocket
        serverListener = Utils.ioExecutor.submit {
            while (true) {
                val client = ClientResponseThread(serverSocket.accept())
                Log.i(TAG, "Bluetooth device at ${client.device.address} connected")
                ScoutingServer.clients.add(client)
                client.start()
                context.sendBroadcast(Intent(INTENT_CLIENT_CONNECTED))
            }
        }

    }

    fun stop() {
        serverListener.cancel(true)
        clients.forEach { it.close() }
        clients.clear()
    }

}