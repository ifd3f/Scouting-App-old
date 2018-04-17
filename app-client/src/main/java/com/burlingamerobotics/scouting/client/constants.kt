package com.burlingamerobotics.scouting.client

/**
 * Broadcast by [ScoutingClientService] when it receives an event from the server.
 */
const val INTENT_CLIENT_EVENT_RECEIVED = "com.burlingamerobotics.scouting.client.EVENT_RCV"

/**
 * The app is started with this intent. It immediately attempts to bind to the server service.
 */
const val INTENT_START_CLIENT_ATTACH_TO_LOCAL = "com.burlingamerobotics.scouting.client.START_ATTACHED_TO_LOCAL"

/**
 * Broadcast by [ScoutingClientService] when it finishes connecting to the server.
 */
const val INTENT_CLIENT_CONNECTED = "com.burlingamerobotics.scouting.client.CLIENT_CONNECTED"

/**
 * Broadcast by [ScoutingClientService] when it finishes disconnecting from the server.
 */
const val INTENT_CLIENT_DISCONNECTED = "com.burlingamerobotics.scouting.client.CLIENT_DISCONNECTED"

