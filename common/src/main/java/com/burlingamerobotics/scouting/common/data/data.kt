package com.burlingamerobotics.scouting.common.data

import java.io.Serializable
import java.util.*

data class TeamPerformance(
        val team: Int,
        val boxes: Int,
        val climbed: Boolean  // TODO POPULATE WITH FIELDS
) : Serializable

sealed class Match(val number: Int) : Serializable

data class MatchTree(val match: Match, val left: MatchTree?, val right: MatchTree?)

class PlannedMatch(number: Int, var red: Array<Int>, var blue: Array<Int>): Match(number) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlannedMatch

        if (!Arrays.equals(red, other.red)) return false
        if (!Arrays.equals(blue, other.blue)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(red)
        result = 31 * result + Arrays.hashCode(blue)
        return result
    }
}

class CompletedMatch(
        number: Int,
        val time: Date,
        var red: Array<TeamPerformance>,
        var blue: Array<TeamPerformance>) : Match(number) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompletedMatch

        if (time != other.time) return false
        if (!Arrays.equals(red, other.red)) return false
        if (!Arrays.equals(blue, other.blue)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + Arrays.hashCode(red)
        result = 31 * result + Arrays.hashCode(blue)
        return result
    }
}

data class Competition(
        val uuid: UUID,
        val name: String,
        val time: Date,
        val qualifiers: Array<Match?>,
        val finals: MatchTree
) : Serializable {
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Competition

        if (uuid != other.uuid) return false
        if (name != other.name) return false
        if (time != other.time) return false
        if (!Arrays.equals(qualifiers, other.qualifiers)) return false
        if (finals != other.finals) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + Arrays.hashCode(qualifiers)
        result = 31 * result + finals.hashCode()
        return result
    }
}

data class CompetitionFileHeader(val uuid: UUID, val name: String, val time: Date)

data class Team(val number: Int, val name: String) : Serializable
