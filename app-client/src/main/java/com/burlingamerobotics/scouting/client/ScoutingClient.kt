package com.burlingamerobotics.scouting.client

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.burlingamerobotics.scouting.common.Listenable
import com.burlingamerobotics.scouting.common.Utils
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.data.Match
import com.burlingamerobotics.scouting.common.protocol.*
import java.io.Closeable
import java.io.IOException
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
    lateinit var listenerThread: ClientListenerThread

    private var _cache: Competition? = null
    private val cacheLock = Object()

    val eventListener: Listenable<Event> = Listenable()

    val cache get(): Competition {
        synchronized(cacheLock) {
            val c = _cache ?: blockingRequest(CompetitionRequest)
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

        listenerThread = ClientListenerThread()
        listenerThread.start()

        invalidateCache()
    }

    fun getQualifiers(): MutableList<Match?> {
        return cache.qualifiers!!
    }

    inline fun <reified T> request(rq: Request<T>): Future<T?> {
        return Utils.ioExecutor.submit(Callable { blockingRequest(rq) })
    }

    fun blockingPost(post: Post) {
        Log.d(TAG, "Posting $post")
        oos.writeObject(post)
    }

    fun post(post: Post): Future<*> {
        return Utils.ioExecutor.submit { blockingPost(post) }
    }

    inline fun <reified T> blockingRequest(rq: Request<T>): T {
        Log.d(TAG, "Requesting with object $rq")
        oos.writeObject(rq)
        Log.d(TAG, "Awaiting response to request")
        val response: Response<*> = listenerThread.pollResponse(10000L)
        Log.d(TAG, "Received response $response")

        if (response.payload is T) {
            return response.payload as T
        } else {
            throw IOException("Response payload ${response.payload} does not correspond to desired type ${T::class}!")
        }
    }

    override fun close() {
        ois.close()
        oos.close()
        socket.close()
        listenerThread.close()
        invalidateCache()
    }

}