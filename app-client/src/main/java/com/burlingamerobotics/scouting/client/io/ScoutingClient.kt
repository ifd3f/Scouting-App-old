package com.burlingamerobotics.scouting.client.io

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.burlingamerobotics.scouting.common.Listenable
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.protocol.*
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * The client's view of the server.
 */
object ScoutingClient : ServerCommStrategy() {
    val TAG = "ScoutingClient"

    lateinit var socket: BluetoothSocket
    lateinit var oos: ObjectOutputStream
    lateinit var ois: ObjectInputStream
    lateinit var device: BluetoothDevice
    lateinit var listenerThread: BluetoothListenerThread

    private var _cache: Competition? = null
    private val cacheLock = Object()

    val eventListener: Listenable<Event> = Listenable()

    val cache get(): Competition {
        synchronized(cacheLock) {
            val c = _cache
                    ?: rawBlockingRequest(CompetitionRequest)
            _cache = c
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

        listenerThread = BluetoothListenerThread()
        listenerThread.start()

        invalidateCache()
    }

    override fun blockingPost(post: Post) {
        Log.d(TAG, "Posting $post")
        oos.writeObject(post)
    }

    override fun <T> rawBlockingRequest(rq: Request<T>): T {
        Log.d(TAG, "Requesting with object $rq")
        oos.writeObject(rq)
        Log.d(TAG, "Awaiting response to request")
        val response: Response<*> = listenerThread.pollResponse(10000L)
        Log.d(TAG, "Received response $response")

        return response.payload as T
    }

    override fun close() {
        ois.close()
        oos.close()
        socket.close()
        listenerThread.close()
        invalidateCache()
    }

}