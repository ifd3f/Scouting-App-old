package com.burlingamerobotics.scouting.client.io

import android.os.Binder
import android.util.Log
import com.burlingamerobotics.scouting.shared.protocol.*
import java.util.concurrent.TimeoutException

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

    fun blockingAction(action: Action): ActionResult {
        return parent.request(action).payload as ActionResult
    }

    fun disconnect() {
        Log.i(TAG, "We're disconnecting")
        try {
            blockingAction(DisconnectAction())
        } catch (e: TimeoutException) {
            Log.e(TAG, "Oh no! We timed out!", e)
        } finally {
            parent.close()
        }
    }

}