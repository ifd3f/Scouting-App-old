package com.burlingamerobotics.scouting.common.data

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class TeamPerformance(
        val team: Int,
        val boxes: Int,
        val climbed: Boolean  // TODO POPULATE WITH FIELDS
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 92387465
    }
}

sealed class Match(val number: Int) : Serializable

data class MatchTree(val match: Match?, val left: MatchTree?, val right: MatchTree?) : Serializable {
    companion object {
        fun generateTournament(rounds: Int): MatchTree {
            assert(rounds > 0)
            if (rounds == 1) {
                return MatchTree(null, null, null)
            }
            val newRounds = rounds - 1
            return MatchTree(null, generateTournament(newRounds), generateTournament(newRounds))
        }

        private const val serialVersionUID: Long = 3546987
    }

}

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
    companion object {
        private const val serialVersionUID: Long = 8935476
    }

}

class CompletedMatch(
        number: Int,
        val time: Date,
        var red: List<TeamPerformance>,
        var blue: List<TeamPerformance>) : Match(number) {
    companion object {
        private const val serialVersionUID: Long = 234905687
    }
}

class Competition(
        val uuid: UUID,
        val name: String,
        cal: Calendar,
        val qualifiers: MutableList<Match?>,
        val finals: MatchTree
) : Serializable {

    val date = cal.time

    fun getHeader(): CompetitionFileHeader = CompetitionFileHeader(uuid, name, date, qualifiers.size)

    fun getFilename(): String = "$uuid.dat"

    companion object {
        private const val serialVersionUID: Long = 1793484567
    }

}

class CompetitionFileHeader(val uuid: UUID, val name: String, val date: Date, val qualifiers: Int) : Serializable {

    fun getTitle() = "$name (${SimpleDateFormat.getDateInstance().format(date.time)})"
    companion object {
        private const val serialVersionUID: Long = 1723544567
    }
}

data class Team(val number: Int, val name: String) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 7928534234
    }
}
