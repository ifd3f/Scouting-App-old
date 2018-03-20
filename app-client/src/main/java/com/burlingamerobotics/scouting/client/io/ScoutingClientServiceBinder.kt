package com.burlingamerobotics.scouting.client.io

import android.os.Binder
import com.burlingamerobotics.scouting.common.protocol.Post
import com.burlingamerobotics.scouting.common.protocol.Request

class ScoutingClientServiceBinder(private val parent: ScoutingClientService) : Binder() {

    fun <T> blockingRequest(request: Request<T>): T {
        return parent.request(request).payload as T
    }

    fun post(post: Post) {
        parent.post(post)
    }

    fun connectTo(server: ServerData) {
        parent.connectTo(server)
    }

}