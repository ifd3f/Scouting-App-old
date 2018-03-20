package com.burlingamerobotics.scouting.server.io

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * The server's view of the client.
 */
class BluetoothClientInterface(private val btSocket: BluetoothSocket) : ScoutingClientInterface(), Runnable {

    val device: BluetoothDevice = btSocket.remoteDevice
    private val TAG: String = "BluetoothInterface[${device.address}]"

    private var thread: Thread? = null

    private lateinit var ois: ObjectInputStream
    private lateinit var oos: ObjectOutputStream

    private var inputListener: ClientInputListener? = null

    override val displayName get(): String = device.name

    override fun attachClientInputListener(listener: ClientInputListener) {
        inputListener = listener
    }

    override fun begin() {
        Log.d(TAG, "Starting listening thread")
        val thread = Thread(this)
        thread.isDaemon = true
        thread.start()
        this.thread = thread
    }

    override fun sendObject(obj: Serializable) {
        Log.d(TAG, "Sending to client: $obj")
        oos.writeObject(obj)
    }

    override fun run() {
        Log.d(TAG, "Starting object streams")
        ois = ObjectInputStream(btSocket.inputStream)
        oos = ObjectOutputStream(btSocket.outputStream)

        Log.i(TAG, "Successfully started!")
        try {
            while (true) {
                val obj = ois.readObject()
                Log.d(TAG, "Received from client: $obj")
                inputListener?.onReceivedFromClient(this, obj) ?: Log.w(TAG, "No listener attached!")
            }
        } catch (ex: InterruptedException) {
            Log.i(TAG, "Interrupted, gracefully stopping thread")
        } catch (ex: IOException) {
            Log.i(TAG, "Client disconnected", ex)
        } finally {
            Log.i(TAG, "Closing socket in finally")
            btSocket.close()
        }
        inputListener?.onClientDisconnected(this)
    }

    override fun close() {
        Log.d(TAG, "Closing socket")
        btSocket.close()
        Log.d(TAG, "Stopping listening thread")
        thread?.interrupt() ?: Log.w(TAG, "There was no listening thread to stop!")
    }

}