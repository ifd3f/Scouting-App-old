package com.burlingamerobotics.scouting.client.io

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import com.burlingamerobotics.scouting.common.INTENT_START_SCOUTING_CLIENT_BLUETOOTH
import com.burlingamerobotics.scouting.common.INTENT_START_SCOUTING_CLIENT_LOCAL
import java.io.Closeable

abstract class ServerCommStrategy : Closeable {

    protected var listener: CommStrategyListener? = null

    fun attachListener(listener: CommStrategyListener?) {
        this.listener = listener
    }

    abstract fun onStart(): Boolean

    abstract fun sendObject(obj: Any)
}

interface CommStrategyListener {

    fun onReceivedObject(obj: Any)

}

interface ServerData {
    val displayName: String
    fun getStartServiceIntent(context: Context): Intent?
}

class BluetoothServerData(val device: BluetoothDevice) : ServerData {
    override val displayName: String = device.name

    override fun getStartServiceIntent(context: Context): Intent? {
        val intent = Intent(context, ClientService::class.java)
        intent.action = INTENT_START_SCOUTING_CLIENT_BLUETOOTH
        intent.putExtra("device", device)
        return intent
    }

    override fun toString(): String = "BluetoothServerData(${device.address})"
}

object LocalServerData : ServerData {
    override val displayName: String = "Local Server"

    override fun getStartServiceIntent(context: Context): Intent? {
        val intent = Intent(context, ClientService::class.java)
        intent.action = INTENT_START_SCOUTING_CLIENT_LOCAL
        return intent
    }

}