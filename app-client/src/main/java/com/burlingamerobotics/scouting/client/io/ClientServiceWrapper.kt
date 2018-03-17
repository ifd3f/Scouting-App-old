package com.burlingamerobotics.scouting.client.io

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import com.burlingamerobotics.scouting.common.data.Team
import com.burlingamerobotics.scouting.common.protocol.Post
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

class ClientServiceWrapper(context: Context) : ServiceConnection, Handler.Callback {
    lateinit var serviceTx: Messenger
    val serviceRx = Messenger(Handler(this))
    val messageQueue = ArrayBlockingQueue<Message>(4)

    init {
        context.bindService(Intent(context, ClientServiceWrapper::class.java), this, 0)
    }

    fun getTeams(): List<Team> {
        serviceRx.send(Message.obtain().also {
            it.what = REQUEST_TEAMS
        })
        return messageQueue.poll(250L, TimeUnit.MILLISECONDS).obj as List<Team>
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

}