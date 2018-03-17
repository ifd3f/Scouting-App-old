package com.burlingamerobotics.scouting.client.io

import java.io.Closeable

abstract class ServerCommStrategy : Closeable {

    protected var listener: CommStrategyListener? = null

    fun attachListener(listener: CommStrategyListener?) {
        this.listener = listener
    }

    abstract fun onStart()

    abstract fun sendObject(obj: Any)
}

interface CommStrategyListener {

    fun onReceivedObject(obj: Any)

}
