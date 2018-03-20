package com.burlingamerobotics.scouting.client.io

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.burlingamerobotics.scouting.common.SCOUTING_UUID
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.concurrent.thread

class BluetoothServerCommStrategy(val device: BluetoothDevice) : ServerCommStrategy() {

    val TAG = "ScoutingClient"

    lateinit var socket: BluetoothSocket
    lateinit var oos: ObjectOutputStream
    lateinit var ois: ObjectInputStream
    lateinit var listenerThread: Thread

    override fun onStart(): Boolean {
        socket = device.createRfcommSocketToServiceRecord(SCOUTING_UUID)
        socket.connect()
        oos = ObjectOutputStream(socket.outputStream)
        ois = ObjectInputStream(socket.inputStream)
        listenerThread = thread(start = true, isDaemon = true) {
            while (true) {
                val obj = ois.readObject()
                Log.d(TAG, "Received $obj from bluetooth")
                listener?.onReceivedObject(obj) ?: Log.w(TAG, "There was no listener to receive it")
            }
        }
        return true
    }

    override fun sendObject(obj: Any) {
        oos.writeObject(obj)
    }

    override fun close() {
        listenerThread.interrupt()
        ois.close()
        oos.close()
        socket.close()
    }
}