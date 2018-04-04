package com.burlingamerobotics.scouting.client.io

import android.os.Binder
import android.util.Log
import com.burlingamerobotics.scouting.common.protocol.Post
import com.burlingamerobotics.scouting.common.protocol.Request

class ScoutingClientServiceBinder(private val parent: ScoutingClientService) : Binder() {

    private val TAG = "ClientServiceBinder"

    fun <T> blockingRequest(request: Request<T>): T {
        Log.d(TAG, "Calling request method on service for request: $request")
        val res = parent.request(request)
        Log.d(TAG, "Got response from service: $res")
        return res.payload as T
    }

    fun post(post: Post) {
        parent.post(post)
    }

    fun connectTo(server: ServerData) {
        parent.connectTo(server)
    }

}