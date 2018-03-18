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
import com.burlingamerobotics.scouting.common.MSG_GIVE_RX
import com.burlingamerobotics.scouting.common.protocol.Post
import com.burlingamerobotics.scouting.common.protocol.Request
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

class ClientServiceWrapper(val context: Context) : ServiceConnection, Handler.Callback, AutoCloseable {

    val uuid: UUID = UUID.randomUUID()
    val TAG = "ClientWrap-${java.lang.Long.toHexString(uuid.mostSignificantBits)}"

    lateinit var serviceTx: Messenger
    val serviceRx = Messenger(Handler(this))
    private val responseQueue = ArrayBlockingQueue<Message>(4)
    private var onBound: Runnable? = null

    fun afterBind(onBound: () -> Unit) {
        bind(Runnable(onBound))
    }

    fun bind(onBound: Runnable? = null) {
        Log.d(TAG, "Binding to ClientService")
        this.onBound = onBound
        context.bindService(Intent(context, ClientService::class.java), this, Service.BIND_NOT_FOREGROUND)
    }

    fun <T> blockingRequest(request: Request<T>): T {
        Log.d(TAG, "Sending request to ClientService: $request")
        serviceTx.send(Message.obtain().apply {
            replyTo = serviceRx
            what = MSG_REQUEST
            obj = request
        })
        val res = responseQueue.poll(250L, TimeUnit.MILLISECONDS).obj
        Log.d(TAG, "Got $res")

        return res as T
    }

    override fun handleMessage(msg: Message): Boolean {
        responseQueue.add(msg)
        return true
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder) {
        Log.d(TAG, "Successfully bound to ClientService!")
        serviceTx = Messenger(service)
        onBound?.run()
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }

    fun post(post: Post) {
        Log.d(TAG, "Sending post to ClientService: $post")
        serviceTx.send(Message.obtain().apply {
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
        serviceTx.send(Message.obtain().apply {
            what = MSG_BEGIN_CLIENT
            replyTo = serviceRx
        })
        val ex = responseQueue.poll(1000L, TimeUnit.MILLISECONDS)?.obj as Throwable?
        if (ex != null) {
            throw ex
        }
    }

}