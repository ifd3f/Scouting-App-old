package com.burlingamerobotics.scouting.client

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.burlingamerobotics.scouting.common.Utils
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.data.CompetitionRequest
import com.burlingamerobotics.scouting.common.data.Match
import com.burlingamerobotics.scouting.common.data.Request
import java.io.Closeable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.Callable
import java.util.concurrent.Future

/**
 * The client's view of the server.
 */
object ScoutingClient : Closeable {

    val TAG = "ScoutingClient"

    lateinit var socket: BluetoothSocket
    lateinit var oos: ObjectOutputStream
    lateinit var ois: ObjectInputStream
    lateinit var device: BluetoothDevice

    private var _cache: Competition? = null
    private val cacheLock = Object()

    val cache get(): Competition {
        synchronized(cacheLock) {
            var c = _cache
            if (c == null) {
                c = blockingRequest(CompetitionRequest)!!
            }
            return c
        }
    }

    fun invalidateCache() {
        _cache = null
    }

    fun start(socket: BluetoothSocket) {
        ScoutingClient.socket = socket
        oos = ObjectOutputStream(socket.outputStream)
        ois = ObjectInputStream(socket.inputStream)
        device = socket.remoteDevice

        invalidateCache()
    }

    fun getQualifiers(): MutableList<Match?> {
        return cache.qualifiers
    }

    fun <T> request(rq: Request<T>): Future<T?> {
        return Utils.ioExecutor.submit(Callable { blockingRequest(rq) })
    }

    fun <T> blockingRequest(rq: Request<T>): T? {
        Log.d(TAG, "Requesting with object $rq")
        oos.writeObject(rq)
        Log.d(TAG, "Awaiting response to request")
        val obj = ois.readObject()
        Log.d(TAG, "Received object of type ${obj.javaClass}: $obj")
        @Suppress("UNCHECKED_CAST")
        return obj as T?
    }

    override fun close() {
        ois.close()
        oos.close()
        socket.close()
        invalidateCache()
    }

}