package com.burlingamerobotics.scouting.server.io

import android.os.Handler
import android.os.Message
import android.os.Messenger
import android.util.Log
import java.io.Serializable


class LocalClientInterface : Handler(), ScoutingClientInterface {
    val TAG = "LocalClientInterface"

    val clientRxTx = Messenger(this)
    var listener: ClientInputListener? = null

    override fun attachClientInputListener(listener: ClientInputListener) {
        this.listener = listener
    }

    override val displayName: String = "Local Client"

    override fun begin() {
        Log.i(TAG, "starting...")
    }

    override fun handleMessage(msg: Message) {
        val obj = msg.obj
        Log.d(TAG, "Received $obj")
        listener?.onReceivedFromClient(this, obj) ?: Log.w(TAG, "  No listener attached to receive it")
    }

    override fun sendObject(obj: Serializable) {
        Log.d(TAG, "Sending $obj")
        clientRxTx.send(Message.obtain().also {
            it.obj = obj
        })
    }

    override fun close() {
        Log.i(TAG, "Closing")
    }

}