package com.burlingamerobotics.scouting.client.io

import android.util.Log
import com.burlingamerobotics.scouting.common.protocol.Event
import com.burlingamerobotics.scouting.common.protocol.Response
import java.io.Closeable
import java.io.IOException
import java.io.ObjectInputStream
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

class BluetoothListenerThread(val commStrat: BluetoothServerCommStrategy) : Thread("ScoutlingThread"), Closeable {
    val TAG = name

    private val responseQueue: BlockingQueue<Response<*>> = ArrayBlockingQueue<Response<*>>(20)

    override fun run() {
        try {
            while (true) {
                val obj = commStrat.ois.readObject()
                Log.d(TAG, "Received object $obj")
                when (obj) {
                    is Response<*> -> {
                        Log.d(TAG, "It's a response, putting it in the response queue")
                        responseQueue.put(obj)
                    }
                    is Event -> {
                        Log.d(TAG, "It's an event, firing event listeners")
                        ScoutingClient.eventListener.fire(obj)
                    }
                    else -> {
                        Log.e(TAG, "Unrecognized type!")
                    }
                }
            }
        } catch (ex: InterruptedException) {
            Log.i(TAG, "Listener thread interrupted, stopping thread", ex)
        } catch (ex: IOException) {
            Log.w(TAG, "Bluetooth socket closed!", ex)
        }
    }

    fun pollResponse(timeout: Long): Response<*> {
        return responseQueue.poll(10000, TimeUnit.MILLISECONDS)
    }

    override fun close() {
        interrupt()
    }

}
