package com.burlingamerobotics.scouting.client.io

import android.bluetooth.BluetoothDevice
import android.content.Context
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
    fun getCommunicationStrategy(context: Context): ServerCommStrategy
}

class BluetoothServerData(val device: BluetoothDevice) : ServerData {
    override val displayName: String = device.name

    override fun getCommunicationStrategy(context: Context): ServerCommStrategy = BluetoothServerCommStrategy(device)

    override fun toString(): String = "BluetoothServerData(${device.address})"
}

object LocalServerData : ServerData {
    override val displayName: String = "Local Server"

    override fun getCommunicationStrategy(context: Context): ServerCommStrategy = LocalServerCommStrategy(context)
}