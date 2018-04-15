package com.burlingamerobotics.scouting.shared

import java.util.*

const val MSG_GIVE_RX = 12421
const val MSG_SEND_OBJ = 2389
const val MSG_DISCONNECT = 5243789

const val API_KEY_TBA = "eeszr95mLfyCJkDqDrXAxsW93MgRYrmn8lLqrUJ76GfSYIWNxA8N5mCUILlX3dFN"

const val PROTOCOL_VERSION = "8"

val SCOUTING_UUID = UUID.nameUUIDFromBytes("ironpanthers-scouting-v${PROTOCOL_VERSION}".toByteArray())

const val URL_TBA_API = "https://www.thebluealliance.com/api/v3/"

const val DURATION_SAVE_DATA = 120000L

const val DISCONNECT_REASON_NONE = 0
const val DISCONNECT_REASON_SHUTDOWN = 23549
const val DISCONNECT_REASON_KICK = 45231
const val DISCONNECT_REASON_BAN = 92837
