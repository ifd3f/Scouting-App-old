package com.burlingamerobotics.scouting.common

import android.content.ComponentName

val COMPONENT_SCOUTING_SERVER_SERVICE = ComponentName(
        "com.burlingamerobotics.scouting.server",
        "com.burlingamerobotics.scouting.server.io.ScoutingServerService")

/**
 * Sent from a local client to the server service initially.
 */
const val INTENT_BIND_LOCAL_CLIENT_TO_SERVER = "com.burlingamerobotics.scouting.common.BIND_LOCAL_CLIENT_TO_SERVER"

const val REQUEST_CODE_NEW = 0xabcd
const val REQUEST_CODE_EDIT = 0xed17
