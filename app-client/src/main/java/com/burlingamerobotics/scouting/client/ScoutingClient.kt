package com.burlingamerobotics.scouting.client

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.burlingamerobotics.scouting.common.Utils
import com.burlingamerobotics.scouting.common.data.*
import java.io.Closeable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.*
import kotlin.concurrent.thread

/**
 * The client's view of the server.
 */
object ScoutingClient : Closeable {

    val TAG = "ScoutingClient"

    lateinit var socket: BluetoothSocket
    lateinit var oos: ObjectOutputStream
    lateinit var ois: ObjectInputStream
    lateinit var device: BluetoothDevice
    lateinit var listenerThread: Thread

    private var _cache: Competition? = null
    private val cacheLock = Object()

    private val responseQueue = ArrayBlockingQueue<Any?>(20)
    private val eventQueue = ArrayBlockingQueue<Any>(20)

    val cache get(): Competition {
        synchronized(cacheLock) {
            val c = _cache ?: blockingRequest(CompetitionRequest)!!
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

        listenerThread = thread(start = true) {

            val TAG = "ScoutlingThread"
            try {
                while (true) {
                    val obj = ois.readObject()
                    Log.d(TAG, "Received object $obj")
                    when (obj) {
                        is Response<*> -> {
                            Log.d(TAG, "It's a response, putting it in the response queue")
                            responseQueue.put(obj.payload)
                        }
                        else -> {
                            Log.e(TAG, "Unrecognized type!")
                        }
                    }
                }
            } catch (ex: InterruptedException) {
                Log.i(TAG, "Listener thread interrupted, stopping thread")
            }

        }

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
        val obj: Any? = responseQueue.poll(10000, TimeUnit.MILLISECONDS)
        Log.d(TAG, "Received object of type ${obj?.javaClass}: $obj")
        @Suppress("UNCHECKED_CAST")
        return obj as T?
    }

    override fun close() {
        ois.close()
        oos.close()
        socket.close()
        listenerThread.interrupt()
        invalidateCache()
    }

}