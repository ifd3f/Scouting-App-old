package com.burlingamerobotics.scouting.common.data

import java.io.Serializable


/**
 * Base interface for an object sent to the server for writing data.
 */
interface Post : Serializable

data class PostMatchInfo(
        val team: Int
) : Post
