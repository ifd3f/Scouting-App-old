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
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

class ClientServiceWrapper(val context: Context) : ServiceConnection, Handler.Callback, AutoCloseable {

    val TAG = "ClientServiceWrapper"

    lateinit var serviceTx: Messenger
    val serviceRx = Messenger(Handler(this))
    val messageQueue = ArrayBlockingQueue<Message>(4)

    fun bind() {
        Log.d(TAG, "Binding to ClientService")
        context.bindService(Intent(context, ClientService::class.java), this, Service.BIND_NOT_FOREGROUND)
        messageQueue.poll(1000L, TimeUnit.MILLISECONDS)
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

    override fun onServiceConnected(name: ComponentName?, service: IBinder) {
        Log.d(TAG, "Successfully bound to service!")
        serviceTx = Messenger(service)
        messageQueue.put(Message.obtain())
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
        Log.d(TAG, "Sending to service")
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