package com.burlingamerobotics.scouting.server

import android.bluetooth.BluetoothSocket

/**
 * The server's view of the client.
 */
class ClientInterface(btSocket: BluetoothSocket) {
    init {
        btSocket.inputStream
    }
}