package com.burlingamerobotics.scouting.server

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
                    is CompetitionRequest -> {
                        Log.d(name, "  It's a request for competition")
                        oos.writeObject(ScoutingServer.competition)
                        //oos.writeObject(listOf(SimpMatch(320, 1), SimpMatch(320, 2), SimpMatch(320, 3)))
                    }
                    is QualifierMatchRequest -> {
                        Log.d(name, "  It's a request for match info")
                        oos.writeObject(ScoutingServer.competition.qualifiers[obj.number])
                        //oos.writeObject(Match(320, 1, listOf(TeamPerformance(10, 10))))
                    }
                    is TeamInfoRequest -> {
                        Log.d(name, "  It's a request for team info")
                        //oos.writeObject(Team())
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