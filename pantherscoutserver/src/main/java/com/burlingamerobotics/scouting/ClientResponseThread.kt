package com.burlingamerobotics.scouting

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.burlingamerobotics.scouting.common.data.*
import java.io.Closeable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * The server's view of the client.
 */
class ClientResponseThread(private val btSocket: BluetoothSocket) : Thread(), Closeable {

    val device = btSocket.remoteDevice

    init {
        name = "ClientInterfaceThread[${device.address}]"
    }

    override fun run() {
        Log.i(name, "Starting ObjectStreams")
        val ois = ObjectInputStream(btSocket.inputStream)
        val oos = ObjectOutputStream(btSocket.outputStream)
        Log.i(name, "Successfully started!")

        try {
            while (true) {
                val obj = ois.readObject()
                Log.d(name, "Received $obj")
                when (obj) {
                    is MatchInfoRequest -> {
                        Log.d(name, "  It's a request for match info")
                        oos.writeObject(Match(320, 1, listOf(TeamPerformance(10, 10))))
                    }
                    is MatchListRequest -> {
                        Log.d(name, "  It's a request for match list")
                        oos.writeObject(listOf(SimpMatch(320, 1), SimpMatch(320, 2), SimpMatch(320, 3)))
                    }
                    else -> {
                        Log.e(name, "Failed to respond to $obj!")
                    }
                }
            }
        } catch (ex: InterruptedException) {
            Log.i(name, "Interrupted, stopping thread")
            ois.close()
            oos.close()
            btSocket.close()
        }

    }

    override fun close() {
        interrupt()
    }

}