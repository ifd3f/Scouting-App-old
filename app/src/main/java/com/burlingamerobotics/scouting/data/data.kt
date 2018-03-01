package com.burlingamerobotics.scouting.data

import java.io.Serializable

data class TeamPerformance(val team: Int, val boxes: Int) : Serializable

data class Match(val competition: Long, val teamPerformance: List<TeamPerformance>) : Serializable

data class Competition(val id: Long) : Serializable

data class Team(val number: Int) : Serializable
