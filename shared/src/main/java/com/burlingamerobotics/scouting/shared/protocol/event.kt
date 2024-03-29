package com.burlingamerobotics.scouting.shared.protocol

import com.burlingamerobotics.scouting.shared.data.Match
import com.burlingamerobotics.scouting.shared.data.Team
import java.io.Serializable

/**
 * Base class for anything that the server would want to broadcast to all clients.
 */
interface Event : Serializable

data class EventTeamChange(val team: Team) : Event

data class EventMatchChange(val number: Int, val match: Match) : Event

data class EventChatMessage(val from: String, val message: String) : Event

/**
 * Usually would only be sent to one dude.
 */
data class EventForceDisconnect(val reason: Int) : Event