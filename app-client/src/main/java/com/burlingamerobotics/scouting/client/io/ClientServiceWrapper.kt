package com.burlingamerobotics.scouting.client.io

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import com.burlingamerobotics.scouting.common.protocol.Post
import com.burlingamerobotics.scouting.common.protocol.Request
import com.burlingamerobotics.scouting.common.protocol.Response
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

class ClientServiceWrapper(val context: Context) : ServiceConnection, Handler.Callback, AutoCloseable {

    val uuid: UUID = UUID.randomUUID()
    private val TAG = "ClientWrap-${java.lang.Long.toHexString(uuid.mostSignificantBits)}"

    private lateinit var tx: Messenger
    private val rx = Messenger(Handler(this))
    private val msgQueue = ArrayBlockingQueue<Message>(4)
    private var onBound: Runnable? = null

    fun bind(onBound: () -> Unit) {
        bind(Runnable(onBound))
    }

    fun bind(onBound: Runnable? = null) {
        Log.d(TAG, "Binding to ClientService")
        this.onBound = onBound
        context.bindService(Intent(context, ClientService::class.java), this, Service.BIND_NOT_FOREGROUND)
    }

    fun <T> blockingRequest(request: Request<T>): T {
        Log.d(TAG, "Sending request to ClientService: $request")
        tx.send(Message.obtain().apply {
            replyTo = rx
            what = MSG_REQUEST
            obj = request
        })
        Log.d(TAG, "Expecting reply on RX: $rx")
        val res = msgQueue.poll(10000L, TimeUnit.MILLISECONDS).obj as Response<T>
        Log.d(TAG, "Received from server: $res")
        return res.payload
    }

    override fun handleMessage(msg: Message): Boolean {
        Log.d(TAG, "Received from ClientService: $msg")
        msgQueue.add(msg)
        return true
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder) {
        Log.d(TAG, "Successfully bound to ClientService!")
        tx = Messenger(service)
        onBound?.run()
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }

    fun post(post: Post) {
        Log.d(TAG, "Sending post to ClientService: $post")
        tx.send(Message.obtain().apply {
            what = MSG_POST
            obj = post
        })
    }

    override fun close() {
        Log.d(TAG, "Unbinding from ClientService")
        context.unbindService(this)
    }

    fun connect() {
        Log.d(TAG, "Requesting ClientService to connect")
        tx.send(Message.obtain().apply {
            what = MSG_BEGIN_CLIENT
            replyTo = rx
        })
        val ex = msgQueue.poll(1000L, TimeUnit.MILLISECONDS)?.obj as Throwable?
        if (ex != null) {
            throw ex
        }
    }

}