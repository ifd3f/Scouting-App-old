package com.burlingamerobotics.scouting.server.io

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import com.burlingamerobotics.scouting.common.send
import com.burlingamerobotics.scouting.shared.data.Competition
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ScoutingServerServiceWrapper : ServiceConnection, Handler.Callback {

    private val TAG = "ServerServiceWrapper"

    private lateinit var tx: Messenger
    private lateinit var rx: Messenger
    private lateinit var handlerThread: Thread

    private val messageQueue = ArrayBlockingQueue<Any?>(10)

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.d(TAG, "Connected to service $name")
        tx = Messenger(service)
        thread(isDaemon = true) {
            Log.d(TAG, "Created handler thread in thread ${Thread.currentThread().id}")
            Looper.prepare()
            rx = Messenger(Handler(this))
            tx.send(Message.obtain().apply {
                what = MSG_PING
                replyTo = rx
            })
            Looper.loop()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        handlerThread.interrupt()
    }

    override fun handleMessage(msg: Message?): Boolean {
        return when (msg!!.what) {
            MSG_PONG -> {
                Log.d(TAG, "We got a pong! we usually don't get any fucking messages at all")
                true
            }
            MSG_RESPONSE -> {
                val obj = msg.data.getSerializable("result")
                Log.d(TAG, "Received message, putting in queue: $obj")
                messageQueue.add(obj)
                true
            }
            else -> false
        }
    }

    fun getCompetition(): Competition {
        Log.d(TAG, "Sending a request for competition data, replyTo = $rx")
        tx.send {
            what = REQUEST_COMPETITION
            replyTo = rx
        }
        Log.d(TAG, "Awaiting response from ServerService")
        val response = messageQueue.poll(10000L, TimeUnit.MILLISECONDS)
        Log.d(TAG, "Got response $response")
        return response as Competition
    }

}