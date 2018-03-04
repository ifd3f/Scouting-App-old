package com.burlingamerobotics.scouting.common

import java.text.SimpleDateFormat
import java.util.*

object Constants {

    val PROTOCOL_VERSION = "1.0.06"

    val SCOUTING_UUID = UUID.nameUUIDFromBytes("ironpanthers-scouting-v${PROTOCOL_VERSION}".toByteArray())

}
