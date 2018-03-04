package com.burlingamerobotics.scouting.common.protocol

import com.burlingamerobotics.scouting.common.data.Team
import java.io.Serializable


/**
 * Base interface for an object sent to the server for writing data.
 */
interface Post : Serializable

data class PostMatchInfo(
        val team: Int
) : Post

data class PostTeamInfo(val team: Team) : Post

data class PostChatMessage(val message: String) : Post
