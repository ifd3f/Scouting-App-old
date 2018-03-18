package com.burlingamerobotics.scouting.client.io

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import com.burlingamerobotics.scouting.common.protocol.Post
import com.burlingamerobotics.scouting.common.protocol.Request
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

class ClientServiceWrapper(val context: Context) : ServiceConnection, Handler.Callback, AutoCloseable {

    lateinit var serviceTx: Messenger
    val serviceRx = Messenger(Handler(this))
    val messageQueue = ArrayBlockingQueue<Message>(4)

    init {
        context.bindService(Intent(context, ClientServiceWrapper::class.java), this, 0)
    }

    fun <T> blockingRequest(request: Request<T>): T {
        serviceTx.send(getMessage().apply {
            what = MSG_REQUEST
            obj = request
        })
        return messageQueue.poll(250L, TimeUnit.MILLISECONDS).obj as T
    }

    override fun handleMessage(msg: Message): Boolean {
        messageQueue.add(msg)
        return true
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        serviceTx = Messenger(service)
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }

    fun post(post: Post) {
        serviceTx.send(Message.obtain().apply {
            what = MSG_POST
            obj = post
        })
    }

    override fun close() {
        context.unbindService(this)
    }

    fun connect() {
        serviceTx.send(getMessage().apply {
            what = MSG_BEGIN_CLIENT
        })
        val ex = messageQueue.poll(250L, TimeUnit.MILLISECONDS).obj as Throwable?
        if (ex != null) {
            throw ex
        }
    }

    private fun getMessage(): Message = Message.obtain().apply { replyTo = serviceRx }

}