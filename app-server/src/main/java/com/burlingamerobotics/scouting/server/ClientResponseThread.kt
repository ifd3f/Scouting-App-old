package com.burlingamerobotics.scouting.server

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.burlingamerobotics.scouting.common.data.*
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

    init {
        name = "ClientInterfaceThread[${device.address}]"
        TAG = name
    }

    override fun run() {
        Log.i(TAG, "Starting ObjectStreams")
        val ois = ObjectInputStream(btSocket.inputStream)
        val oos = ObjectOutputStream(btSocket.outputStream)

        try {
            Log.i(TAG, "Successfully started!")
            while (true) {
                val request = ois.readObject() as Request<*>
                Log.d(TAG, "Received $request")
                val response = getItemByRequest(request)
                Log.d(TAG, "Writing $response")
                oos.writeObject(response)
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

    fun <T> getItemByRequest(request: Request<T>): Any? {
        return when (request) {
            is CompetitionRequest -> {
                Log.d(TAG, "It's a request for all competition data")
                ScoutingServer.competition
            }
            is QualifierMatchRequest -> {
                Log.d(TAG, "It's a request for .match info")
                ScoutingServer.competition.qualifiers[request.number]
            }
            is TeamInfoRequest -> {
                Log.d(TAG, "It's a request for team info")
                db.getTeam(request.team)
            }
            is TeamListRequest -> {
                Log.d(TAG, "It's a request for a list of teams")
                db.listTeams()
            }
            else -> {
                Log.e(TAG, "Failed to respond to $request!")
                null
            }
        }
    }

    override fun close() {
        interrupt()
    }

}