package com.burlingamerobotics.scouting.data

import java.io.Serializable

data class TeamPerformance(val team: Int, val boxes: Int) : Serializable

data class Match(val competition: Long, val number: Int, val teamPerformance: List<TeamPerformance>) : Serializable {
    fun simplified(): SimpMatch = SimpMatch(competition, number)
}

data class SimpMatch(val competition: Long, val number: Int) : Serializable

data class Competition(val id: Long) : Serializable

data class Team(val number: Int) : Serializable
