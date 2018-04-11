package com.burlingamerobotics.scouting.shared.protocol

import com.burlingamerobotics.scouting.shared.data.Team
import com.burlingamerobotics.scouting.shared.data.TeamPerformance
import java.io.Serializable


/**
 * Base interface for an object sent to the server for writing data.
 */
interface Post : Serializable

data class PostTeamPerformance(
        val team: Int,
        val teamPerformance: TeamPerformance
) : Post

data class PostTeamInfo(val team: Team) : Post

data class PostChatMessage(val message: String) : Post
