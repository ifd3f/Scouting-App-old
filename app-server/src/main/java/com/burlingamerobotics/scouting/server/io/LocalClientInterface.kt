package com.burlingamerobotics.scouting.server.io

import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import com.burlingamerobotics.scouting.shared.MSG_GIVE_RX
import com.burlingamerobotics.scouting.shared.MSG_SEND_OBJ
import java.io.Serializable


class LocalClientInterface : Handler.Callback, ScoutingClientInterface() {
    private val TAG = "LocalClientInterface"
    override val uniqueId: String = "__LOCAL__"

    private lateinit var tx: Messenger
    private val rx = Messenger(Handler(this))
    private var listener: ClientInputListener? = null

    val binder: IBinder get() = rx.binder

    override fun attachClientInputListener(listener: ClientInputListener) {
        this.listener = listener
    }

    override val displayName: String = "Local Client"

    override fun begin() {
        Log.i(TAG, "Starting")
    }

    override fun handleMessage(msg: Message): Boolean {
        Log.d(TAG, "Received from ClientService: $msg")
        when (msg.what) {
            MSG_GIVE_RX -> {
                Log.d(TAG,"  Received a RX messenger")
                tx = msg.replyTo
            }
            MSG_SEND_OBJ -> {
                val obj = msg.data.getSerializable("object")
                Log.d(TAG, "  Unpacked object from ClientService: $obj")
                listener?.onReceivedFromClient(this, obj) ?: Log.w(TAG, "No listener attached to receive it!")
            }
            else -> {
                Log.e(TAG, "  Could not process ${msg.what}!")
            }
        }
        return true
    }

    override fun sendObject(obj: Serializable) {
        Log.d(TAG, "Sending to ClientService: $obj")
        tx.send(Message.obtain().also {
            it.data.putSerializable("object", obj)
        })
    }

    override fun close() {
        Log.i(TAG, "Closing")
    }

}