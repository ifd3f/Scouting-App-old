package com.burlingamerobotics.scouting.server.io

import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

class ScoutingServerServiceWrapper(binder: IBinder) : Handler.Callback {

    private val serviceTx = Messenger(binder)
    private val serviceRx = Messenger(Handler(this))
    private val messageQueue = ArrayBlockingQueue<Message>(4)

    fun getConnectedClients(): List<ClientInfo> {
        serviceTx.send(Message.obtain().also {
            it.what = CONNECTED_CLIENTS
        })
        return messageQueue.poll(250L, TimeUnit.MILLISECONDS).obj as List<ClientInfo>
    }

    override fun handleMessage(msg: Message?): Boolean {
        messageQueue.add(msg)
        return true
    }

}