package com.burlingamerobotics.scouting.common

import android.content.ComponentName

val COMPONENT_SCOUTING_SERVER_SERVICE = ComponentName(
        "com.burlingamerobotics.scouting.server",
        "com.burlingamerobotics.scouting.server.io.ScoutingServerService")

const val INTENT_CLIENT_RESPONSE_TO_MESSAGE = "com.burlingamerobotics.scouting.RESPONSE_TO_MESSAGE"
const val INTENT_CLIENT_EVENT_RECEIVED = "com.burlingamerobotics.scouting.EVENT_RCV"

const val INTENT_START_CLIENT_ATTACH_TO_LOCAL = "com.burlingamerobotics.scouting.START_CLIENT_ATTACHED_TO_LOCAL"

const val INTENT_START_SCOUTING_CLIENT_LOCAL = "com.burlingamerobotics.scouting.START_LOCAL_CLIENT"
const val INTENT_START_SCOUTING_CLIENT_BLUETOOTH = "com.burlingamerobotics.scouting.START_BLUETOOTH_CLIENT"

const val INTENT_BIND_LOCAL_CLIENT_TO_SERVER = "com.burlingamerobotics.scouting.BIND_LOCAL_CLIENT_TO_SERVER"
const val INTENT_BIND_SERVER_WRAPPER = "com.burlingamerobotics.scouting.BIND_SERVER_WRAPPER"

const val INTENT_CLIENT_CONNECTED = "com.burlingamerobotics.scouting.SERVER_CLIENT_CONNECTED"
const val INTENT_CLIENT_DISCONNECTED = "com.burlingamerobotics.scouting.SERVER_CLIENT_DISCONNECTED"
const val INTENT_START_SCOUTING_SERVER = "com.burlingamerobotics.scouting.START_SERVER"

const val REQUEST_CODE_NEW = 0xabcd
const val REQUEST_CODE_EDIT = 0xed17
