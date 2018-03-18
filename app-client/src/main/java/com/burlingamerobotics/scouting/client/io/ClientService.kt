package com.burlingamerobotics.scouting.client.io

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import com.burlingamerobotics.scouting.common.*
import com.burlingamerobotics.scouting.common.protocol.Event
import com.burlingamerobotics.scouting.common.protocol.Post
import com.burlingamerobotics.scouting.common.protocol.Request
import com.burlingamerobotics.scouting.common.protocol.Response
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * It's a service that keeps track of the server-side data.
 */
class ClientService : Service(), CommStrategyListener, Handler.Callback {

    val TAG = "ClientService"
    val responseQueue: ArrayBlockingQueue<Response<*>> = ArrayBlockingQueue(16)

    lateinit var serverCommStrategy: ServerCommStrategy

    /**
     * Handle messages for bound contexts.
     */
    override fun handleMessage(msg: Message): Boolean {
        Log.i(TAG, "Received $msg from ${msg.replyTo}")
        when (msg.what) {
            MSG_BEGIN_CLIENT -> {
                Log.d(TAG, "  Received a command to start!")
                try {
                    serverCommStrategy.onStart()
                } catch (e: Exception) {
                    Log.e(TAG, "  Failed to connect!", e)
                    msg.replyTo.send(Message.obtain().apply {
                        obj = e
                    })
                }
            }
            MSG_REQUEST -> {
                val rq = msg.obj as Request<*>
                Log.d(TAG, "  Message is a request: $rq")
                Utils.ioExecutor.submit {
                    Log.d(TAG, "  Sending request to strategy")
                    serverCommStrategy.sendObject(rq)
                    msg.replyTo.send(Message.obtain().apply {
                        val received = responseQueue.poll(10000L, TimeUnit.MILLISECONDS)
                        Log.d(TAG, "  Received $received, sending to replyTo")
                        what = MSG_RESPONSE
                        obj = received
                    })
                }
            }
            MSG_POST -> {
                val post = msg.obj as Post
                Log.d(TAG, "  Message is a post: $post")
                serverCommStrategy.sendObject(post)
            }
            else -> {
                throw UnsupportedOperationException("Cannot use ${msg.what}!")
            }
        }
        return true
    }

    override fun onBind(intent: Intent): IBinder {
        return Messenger(Handler(this)).binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            INTENT_START_SCOUTING_CLIENT_BLUETOOTH -> {
                val device = intent.getParcelableExtra<BluetoothDevice>("device")
                serverCommStrategy = BluetoothServerCommStrategy(device)
                serverCommStrategy.attachListener(this)
            }
            INTENT_START_SCOUTING_CLIENT_LOCAL -> {
                serverCommStrategy = LocalServerCommStrategy(this)
                serverCommStrategy.attachListener(this)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onReceivedObject(obj: Any) {
        when (obj) {
            is Event -> {
                sendBroadcast(Intent(INTENT_CLIENT_EVENT_RECEIVED).apply {
                    putExtra("event", obj)
                })
            }
            is Response<*> -> {
                responseQueue.add(obj)
            }
        }
    }

}