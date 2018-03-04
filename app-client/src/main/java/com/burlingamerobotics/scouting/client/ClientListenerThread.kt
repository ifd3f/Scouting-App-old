package com.burlingamerobotics.scouting.client

import android.util.Log
import com.burlingamerobotics.scouting.common.protocol.Event
import com.burlingamerobotics.scouting.common.protocol.Response
import java.io.Closeable
import java.util.concurrent.BlockingQueue

class ClientListenerThread(val responseQueue: BlockingQueue<Any?>, val eventQueue: BlockingQueue<Event>) : Thread("ScoutlingThread"), Closeable {
    val TAG = name

    override fun run() {
        try {
            while (true) {
                val obj = ScoutingClient.ois.readObject()
                Log.d(TAG, "Received object $obj")
                when (obj) {
                    is Response<*> -> {
                        Log.d(TAG, "It's a response, putting it in the response queue")
                        responseQueue.put(obj.payload)
                    }
                    is Event -> {
                        Log.d(TAG, "It's an event, putting it in the event queue")
                        eventQueue.put(obj)
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

    override fun close() {
        interrupt()
    }

}
