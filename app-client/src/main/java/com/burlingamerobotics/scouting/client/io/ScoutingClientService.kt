package com.burlingamerobotics.scouting.client.io

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.burlingamerobotics.scouting.common.INTENT_CLIENT_EVENT_RECEIVED
import com.burlingamerobotics.scouting.common.protocol.Event
import com.burlingamerobotics.scouting.common.protocol.Post
import com.burlingamerobotics.scouting.common.protocol.Request
import com.burlingamerobotics.scouting.common.protocol.Response
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * It's a service that keeps track of the server-side matchData.
 */
class ScoutingClientService : Service(), CommStrategyListener {

    private val TAG = "ScoutingClientService"
    private val responseQueue: ArrayBlockingQueue<Response<*>> = ArrayBlockingQueue(16)

    private lateinit var serverCommStrategy: ServerCommStrategy

    internal fun connectTo(server: ServerData) {
        serverCommStrategy = server.getCommunicationStrategy(this)
        try {
            Log.d(TAG, "Asking strategy to connect")
            serverCommStrategy.onStart()
            serverCommStrategy.attachListener(this)
            Log.i(TAG, "Successfully connected to server!")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect!", e)
            throw e
        }
    }

    internal fun request(rq: Request<*>): Response<*> {
        Log.d(TAG, "Sending request to strategy: $rq")
        serverCommStrategy.sendObject(rq)
        for (i in 1..3) {
            Log.d(TAG, "Polling attempt $i/3")
            val res = responseQueue.poll(10000L, TimeUnit.MILLISECONDS)
            Log.d(TAG, "Received from queue: $res")
            when {
                res == null -> Log.w(TAG, "")
                rq.uuid == res.to -> return res
                else -> {
                    Log.w(TAG, "It's not a response to the one we want. Putting in queue and trying again")
                    responseQueue.put(res)
                }
            }
        }
        throw TimeoutException("Did not receive a response in time!")
    }

    internal fun post(post: Post) {
        Log.d(TAG, "Sending post to strategy: $post")
        serverCommStrategy.sendObject(post)
    }

    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG, "Someone wants to bind to me")
        return ScoutingClientServiceBinder(this)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Someone wants to unbind from me")
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Starting")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Stopping")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onReceivedObject(obj: Any) {
        Log.d(TAG, "Received object from strategy: $obj")
        when (obj) {
            is Event -> {
                Log.d(TAG, "Object is event, broadcasting")
                sendBroadcast(Intent(INTENT_CLIENT_EVENT_RECEIVED).apply {
                    putExtra("event", obj)
                })
            }
            is Response<*> -> {
                Log.d(TAG, "Object is response, adding to queue")
                responseQueue.add(obj)
            }
        }
    }

}