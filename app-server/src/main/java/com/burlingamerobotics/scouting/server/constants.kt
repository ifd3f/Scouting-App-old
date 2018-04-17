package com.burlingamerobotics.scouting.server

/**
 * Sent from the [ScoutingServerServiceWrapper] to [ScoutingServerService] to request to bind.
 */
const val INTENT_BIND_SERVER_WRAPPER = "com.burlingamerobotics.scouting.server.BIND_SERVER_WRAPPER"

/**
 * Broadcast by [ScoutingServerService] when a dude connects.
 */
const val INTENT_SERVER_CLIENT_CONNECTED = "com.burlingamerobotics.scouting.server.CLIENT_CONNECTED"

/**
 * Broadcast by [ScoutingServerService] when a dude disconnects.
 */
const val INTENT_SERVER_CLIENT_DISCONNECTED = "com.burlingamerobotics.scouting.server.CLIENT_DISCONNECTED"

/**
 * Sent from whoever to [ScoutingServerService] to request to start.
 */
const val INTENT_SERVER_START_SCOUTING_SERVER = "com.burlingamerobotics.scouting.server.START_SERVER"
