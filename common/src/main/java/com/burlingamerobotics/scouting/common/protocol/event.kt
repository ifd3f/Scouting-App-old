package com.burlingamerobotics.scouting.common.protocol

import com.burlingamerobotics.scouting.common.data.Match
import com.burlingamerobotics.scouting.common.data.Team
import java.io.Serializable

/**
 * Base class for anything that the server would want to broadcast to all clients.
 */
interface Event : Serializable

data class EventTeamChange(val team: Team) : Event

data class EventMatchChange(val number: Int, val match: Match)

data class EventChatMessage(val from: String, val message: String)
