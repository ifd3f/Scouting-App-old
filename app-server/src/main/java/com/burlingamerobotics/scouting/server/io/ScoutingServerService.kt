package com.burlingamerobotics.scouting.server.io

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import com.burlingamerobotics.scouting.common.INTENT_BIND_LOCAL_CLIENT_TO_SERVER
import java.io.Serializable


class ScoutingServerService : Service() {

    val TAG = "ScoutingServerService"

    private val clients = mutableListOf<ScoutingClient>();

    override fun onBind(intent: Intent): IBinder {
        when (intent.action) {
            INTENT_BIND_LOCAL_CLIENT_TO_SERVER -> {
                Log.i(TAG, "Local client wants to bind to server")
            }
        }
        val newClient = LocalClientHandler()
        clients.add(newClient)
        return newClient.messenger.binder
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")

    }

}

class LocalClientHandler : Handler(), ScoutingClient {
    val TAG = "LocalClientHandler"

    val messenger = Messenger(this)
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
        listener?.onClientSentObject(this, obj) ?: Log.w(TAG, "  No listener attached!")
    }

    override fun sendObject(obj: Serializable) {
        Log.d(TAG, "Sending $obj")
        messenger.send(Message.obtain().also {
            it.obj = obj
        })
    }

    override fun close() {

    }

}