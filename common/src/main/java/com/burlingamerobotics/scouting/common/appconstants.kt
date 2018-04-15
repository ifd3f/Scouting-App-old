package com.burlingamerobotics.scouting.common

import android.content.ComponentName

val COMPONENT_SCOUTING_SERVER_SERVICE = ComponentName(
        "com.burlingamerobotics.scouting.server",
        "com.burlingamerobotics.scouting.server.io.ScoutingServerService")

const val INTENT_BIND_LOCAL_CLIENT_TO_SERVER = "com.burlingamerobotics.scouting.common.BIND_LOCAL_CLIENT_TO_SERVER"
const val INTENT_SERVER_CLIENT_CONNECTED = "com.burlingamerobotics.scouting.common.SERVER_CLIENT_CONNECTED"
const val REQUEST_CODE_NEW = 0xabcd
const val REQUEST_CODE_EDIT = 0xed17
