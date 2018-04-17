package com.burlingamerobotics.scouting.client.io

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import com.burlingamerobotics.scouting.common.COMPONENT_SCOUTING_SERVER_SERVICE
import com.burlingamerobotics.scouting.common.INTENT_BIND_LOCAL_CLIENT_TO_SERVER
import com.burlingamerobotics.scouting.shared.MSG_GIVE_RX
import com.burlingamerobotics.scouting.shared.MSG_SEND_OBJ
import java.io.Serializable
import kotlin.concurrent.thread

class LocalServerCommStrategy(val context: Context) : ServerCommStrategy(), ServiceConnection, Handler.Callback {

    private val TAG = "LocalServerComm"

    private lateinit var rx: Messenger
    private lateinit var tx: Messenger

    private lateinit var rxListenerThread: Thread

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
        tx = Messenger(service)
        rxListenerThread = thread(isDaemon = true) {
            Looper.prepare()
            rx = Messenger(Handler(this))
            Log.d(TAG, "Sending our RX messenger")
            tx.send(Message.obtain().apply {
                what = MSG_GIVE_RX
                replyTo = rx
            })
            Looper.loop()
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {

    }

    /**
     * Handle messages coming from server.
     */
    override fun handleMessage(msg: Message): Boolean {
        Log.d(TAG, "Received from ServerService: $msg")
        val obj = msg.data.getSerializable("object")!!
        Log.d(TAG, "Unpacked object $obj")
        listener?.onReceivedObject(obj) ?: Log.w(TAG, "There was no listener to receive it")
        return true
    }

    override fun sendObject(obj: Any) {
        Log.d(TAG, "Preparing message with serialized $obj")
        val msg = Message.obtain()
        msg.what = MSG_SEND_OBJ
        msg.replyTo = rx
        msg.data.putSerializable("object", obj as Serializable)
        Log.d(TAG, "  Sending to ServerService: $msg")
        tx.send(msg)
    }

    override fun close() {
        rxListenerThread.interrupt()
    }

}
