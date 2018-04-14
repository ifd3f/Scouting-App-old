package com.burlingamerobotics.scouting.shared.protocol

import com.burlingamerobotics.scouting.shared.data.TeamPerformance
import java.io.Serializable

sealed class Action : Serializable

data class EditTeamPerformanceAction(val match: Int, val team: Int) : Action()

data class EndEditTeamPerformanceAction(val match: Int, val team: Int, val teamPerformance: TeamPerformance?) : Action()

data class ActionResult(val status: Boolean, val payload: Any? = null) : Serializable
