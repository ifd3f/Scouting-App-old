package com.burlingamerobotics.scouting.common.protocol

import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.data.Match
import com.burlingamerobotics.scouting.common.data.Team
import java.io.Serializable

/**
 * Base interface for an object sent to the server for querying.
 */
interface Request<ResponseType> : Serializable

/**
 * A response to a [Request].
 */
data class Response<T>(val payload: T) : Serializable

object CompetitionRequest : Request<Competition>

object TeamListRequest : Request<List<Team>>

data class QualifierMatchRequest(val number: Int) : Request<Match>

data class TeamInfoRequest(val team: Int) : Request<Team>

object Ping : Request<Unit>
