package com.burlingamerobotics.scouting.client

import android.bluetooth.BluetoothSocket
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * The client's view of the server.
 */
class ServerInterface(socket: BluetoothSocket) {

    val oos = ObjectOutputStream(socket.outputStream)
    val ois = ObjectInputStream(socket.inputStream)

}