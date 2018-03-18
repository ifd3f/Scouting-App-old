package com.burlingamerobotics.scouting.client.io

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import com.burlingamerobotics.scouting.common.INTENT_BIND_LOCAL_CLIENT_TO_SERVER
import com.burlingamerobotics.scouting.common.COMPONENT_SCOUTING_SERVER_SERVICE

class LocalServerCommStrategy(val context: Context) : ServerCommStrategy(), ServiceConnection, Handler.Callback {

    val TAG = "LocalServerComm"

    lateinit var serviceTx: Messenger
    val serviceRx: Messenger = Messenger(Handler(this))

    override fun onStart(): Boolean {
        val intent = Intent()
        intent.action = INTENT_BIND_LOCAL_CLIENT_TO_SERVER
        intent.component = COMPONENT_SCOUTING_SERVER_SERVICE
        //val intent = Intent("com.burlingamerobotics.scouting.server.io.ScoutingServerService")
        Log.d(TAG, "Binding to server service with $intent")
        val result = context.bindService(intent, this, Service.BIND_ABOVE_CLIENT)
        Log.d(TAG, "Binding result: $result")
        return result
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder) {
        Log.i(TAG, "Successfully bound to server!")
        serviceTx = Messenger(service)
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }

    override fun handleMessage(msg: Message): Boolean {
        Log.d(TAG, "Received from service: $msg")
        listener?.onReceivedObject(msg.obj) ?: Log.w(TAG, "There was no listener to receive it")
        return true
    }

    override fun sendObject(obj: Any) {
        serviceTx.send(Message.obtain().apply {
            replyTo = serviceRx
            this@apply.obj = obj
        })
    }

    override fun close() {

    }

}