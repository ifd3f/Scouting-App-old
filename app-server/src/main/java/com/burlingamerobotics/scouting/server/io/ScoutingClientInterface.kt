package com.burlingamerobotics.scouting.server.io

import java.io.Serializable

abstract class ScoutingClientInterface : AutoCloseable {

    val id: Long = System.nanoTime() * 437985 + System.nanoTime()  // shitty random

    abstract val displayName: String

    abstract fun begin()
    abstract fun sendObject(obj: Serializable)
    abstract fun attachClientInputListener(listener: ClientInputListener)

    fun getInfo() = ClientInfo(id, displayName)

}

interface ClientInputListener {

    /**
     * Called when the client sends over an object.
     */
    fun onReceivedFromClient(client: ScoutingClientInterface, obj: Any)

    /**
     * Called when the client socket is closed, either intentionally or not.
     */
    fun onClientDisconnected(client: ScoutingClientInterface)

}

data class ClientInfo(val id: Long, val displayName: String) : Serializable
