package com.burlingamerobotics.scouting.common

import java.util.*

const val MSG_BEGIN_CLIENT = 24598413
const val MSG_REQUEST = 8193489
const val MSG_RESPONSE = 23578012
const val MSG_POST = 23583857

const val API_KEY_TBA = "eeszr95mLfyCJkDqDrXAxsW93MgRYrmn8lLqrUJ76GfSYIWNxA8N5mCUILlX3dFN"

const val PROTOCOL_VERSION = "1.0.07"

val SCOUTING_UUID = UUID.nameUUIDFromBytes("ironpanthers-scouting-v${PROTOCOL_VERSION}".toByteArray())

const val URL_TBA_API = "https://www.thebluealliance.com/api/v3/"
const val URI_SCOUTING_SERVER_SERVICE = "com.burlingamerobotics.scouting.server.io.ScoutingServerService"

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

const val REQUEST_CODE_NEW_COMPETITION = 2356847
const val REQUEST_CODE_EDIT_COMPETITION = 930214785

const val DURATION_SAVE_DATA = 120000L