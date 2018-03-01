package com.burlingamerobotics.scouting.server

import android.bluetooth.BluetoothSocket
import java.io.Closeable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.Executors

/**
 * The server's view of the client.
 */
class ClientInterface(private val btSocket: BluetoothSocket) : Closeable {

    val ois = ObjectInputStream(btSocket.inputStream)
    val oos = ObjectOutputStream(btSocket.outputStream)
    val device = btSocket.remoteDevice

    fun start() {
        Thread {
            while (true) {
                val obj = ois.readObject()
            }
        }
    }

    override fun close() {
        btSocket.close()
    }

}