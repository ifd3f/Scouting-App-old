package com.burlingamerobotics.scouting.data

import java.io.Serializable

/**
 * Base interface for an object sent to the server for querying.
 */
interface Request<ResponseType> : Serializable

/**
 * Base interface for an object sent to the server for writing data.
 */
interface Post : Serializable

data class MatchListRequest(val competition: Long) : Request<List<SimpMatch>>

data class MatchInfoRequest(val competition: Long, val number: Int) : Request<Match> {
    constructor(sm: SimpMatch) : this(sm.competition, sm.number)
}

data class TeamInfoRequest(val team: Int) : Request<Team>

data class PostMatchInfo(
        val team: Int
) : Post
