package com.burlingamerobotics.scouting.server.io

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * The server's view of the client.
 */
class BluetoothClientInterface(private val btSocket: BluetoothSocket) : Thread(), ScoutingClientInterface {

    val TAG: String
    val device = btSocket.remoteDevice

    lateinit var ois: ObjectInputStream
    lateinit var oos: ObjectOutputStream

    override val displayName get(): String = device.name

    var inputListener: ClientInputListener? = null

    init {
        isDaemon = true
        name = "BluetoothInterface[${device.address}]"
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
                inputListener?.onReceivedFromClient(this, obj) ?: Log.w(TAG, "No listener attached!")
            }
        } catch (ex: InterruptedException) {
            Log.i(TAG, "Interrupted, gracefully stopping thread")
        } catch (ex: IOException) {
            Log.w(TAG, "Client disconnected", ex)
        } finally {
            btSocket.close()
        }
        inputListener?.onClientDisconnected(this)
    }

    override fun close() {
        interrupt()
    }

}