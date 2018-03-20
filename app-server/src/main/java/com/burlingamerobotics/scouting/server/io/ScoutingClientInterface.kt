package com.burlingamerobotics.scouting.server.io

import java.io.Closeable
import java.io.Serializable
import java.util.*

abstract class ScoutingClientInterface : AutoCloseable {

    val uuid = UUID.randomUUID()

    abstract val displayName: String

    abstract fun begin()

    abstract fun sendObject(obj: Serializable)

    abstract fun attachClientInputListener(listener: ClientInputListener)

    fun getInfo() = ClientInfo(uuid, displayName)

}

interface ClientInputListener {

    fun onReceivedFromClient(client: ScoutingClientInterface, obj: Any)

    fun onClientDisconnected(client: ScoutingClientInterface)

}

data class ClientInfo(val uuid: UUID, val displayName: String) : Serializable
