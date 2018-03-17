package com.burlingamerobotics.scouting.server.io

import java.io.Closeable
import java.io.Serializable

interface ScoutingClientInterface : Closeable {

    val displayName: String

    fun begin()

    fun sendObject(obj: Serializable)

    fun attachClientInputListener(listener: ClientInputListener)

}

interface ClientInputListener {

    fun onReceivedFromClient(client: ScoutingClientInterface, obj: Any)

    fun onClientDisconnected(client: ScoutingClientInterface)

}