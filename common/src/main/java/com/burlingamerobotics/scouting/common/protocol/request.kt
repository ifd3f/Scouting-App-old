package com.burlingamerobotics.scouting.common.protocol

import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.data.Match
import com.burlingamerobotics.scouting.common.data.Team
import java.io.Serializable
import java.util.*

/**
 * Base class for an object sent to the server for querying.
 */
abstract class Request<ResponseType> : Serializable {
    val uuid: UUID = UUID.randomUUID()
}

/**
 * A response to a [Request].
 */
data class Response<T>(val payload: T, val to: UUID) : Serializable

class CompetitionRequest : Request<Competition>()

class TeamListRequest : Request<List<Team>>()

data class QualifierMatchRequest(val number: Int) : Request<Match>()

data class TeamInfoRequest(val team: Int) : Request<Team>()

class Ping : Request<Unit>()
