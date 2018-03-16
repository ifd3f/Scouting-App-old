package com.burlingamerobotics.scouting.server.io

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.burlingamerobotics.scouting.common.protocol.*
import java.io.Closeable
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * The server's view of the client.
 */
class ClientResponseThread(private val btSocket: BluetoothSocket, private val db: ScoutingDB)
    : Thread(), ScoutingClient {

    val TAG: String
    val device = btSocket.remoteDevice

    lateinit var ois: ObjectInputStream
    lateinit var oos: ObjectOutputStream

    override val displayName get(): String = device.name

    var inputListener: ClientInputListener? = null

    init {
        name = "ClientInterfaceThread[${device.address}]"
        TAG = name
    }

    override fun attachClientInputListener(listener: ClientInputListener) {
        inputListener = listener
    }

    override fun begin() {
        start()
    }

    override fun sendObject(obj: Serializable) {
        oos.writeObject(obj)
    }

    override fun run() {
        Log.i(TAG, "Starting ObjectStreams")
        ois = ObjectInputStream(btSocket.inputStream)
        oos = ObjectOutputStream(btSocket.outputStream)

        Log.i(TAG, "Successfully started!")
        try {
            while (true) {
                val obj = ois.readObject()
                Log.d(TAG, "Received $obj")
                inputListener?.onClientSentObject(this, obj) ?: Log.w(TAG, "No listener attached!")
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

    override fun close() {
        interrupt()
    }

}