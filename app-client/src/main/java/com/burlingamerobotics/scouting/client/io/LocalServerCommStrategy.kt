package com.burlingamerobotics.scouting.client.io

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
import com.burlingamerobotics.scouting.common.URI_SCOUTING_SERVER_SERVICE

class LocalServerCommStrategy(val context: Context) : ServerCommStrategy(), ServiceConnection, Handler.Callback {

    val TAG = "LocalServerComm"

    lateinit var serviceTx: Messenger
    val serviceRx: Messenger = Messenger(Handler(this))

    override fun onStart() {
        val intent = Intent(INTENT_BIND_LOCAL_CLIENT_TO_SERVER, Uri.parse(URI_SCOUTING_SERVER_SERVICE))
        context.bindService(intent, this, 0)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder) {
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