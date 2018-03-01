package com.burlingamerobotics.scouting.client

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.burlingamerobotics.scouting.Utils
import com.burlingamerobotics.scouting.data.MatchInfoRequest
import com.burlingamerobotics.scouting.data.Request
import java.io.Closeable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.Callable
import java.util.concurrent.Future

/**
 * The client's view of the server.
 */
object ScoutingClient : Closeable {

    lateinit var socket: BluetoothSocket
    lateinit var oos: ObjectOutputStream
    lateinit var ois: ObjectInputStream
    lateinit var device: BluetoothDevice

    fun start(socket: BluetoothSocket) {
        this.socket = socket
        oos = ObjectOutputStream(socket.outputStream)
        ois = ObjectInputStream(socket.inputStream)
        device = socket.remoteDevice
    }

    fun <T> request(rq: Request<T>): Future<T?> {
        oos.writeObject(rq)
        return Utils.ioExecutor.submit(Callable { ois.readObject() as T? })
    }

    override fun close() {
        ois.close()
        oos.close()
        socket.close()
    }

}