package com.burlingamerobotics.scouting.server.io

import com.burlingamerobotics.scouting.common.protocol.Event
import java.io.Closeable
import java.io.Serializable

interface ScoutingClient : Closeable {

    val displayName: String

    fun begin()

    fun sendObject(obj: Serializable)

    fun attachClientInputListener(listener: ClientInputListener)

}

interface ClientInputListener {
    fun onClientSentObject(client: ScoutingClient, obj: Any)
}