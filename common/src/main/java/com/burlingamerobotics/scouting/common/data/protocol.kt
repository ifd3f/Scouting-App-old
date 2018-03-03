package com.burlingamerobotics.scouting.common.data

import java.io.Serializable

/**
 * Base interface for an object sent to the server for querying.
 */
interface Request<ResponseType> : Serializable

/**
 * Base interface for an object sent to the server for writing data.
 */
interface Post : Serializable

object CompetitionRequest : Request<Competition>

data class QualifierMatchRequest(val number: Int) : Request<Match>

data class TeamInfoRequest(val team: Int) : Request<Team>

data class PostMatchInfo(
        val team: Int
) : Post
