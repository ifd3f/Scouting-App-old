package com.burlingamerobotics.scouting.common.data

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

enum class StartPosition : Serializable {
    LEFT, CENTER, RIGHT
}

enum class EndPosition : Serializable {
    NONE, PARK, CLIMB, CLIMB_LEVITATE
}

data class TeamPerformance(
        val team: Int,
        var autoStartPos: StartPosition = StartPosition.CENTER,
        var autoCrossedLine: Boolean = false,
        var autoCubesOwnSwitch: Int = 0,
        var autoCubesScale: Int = 0,
        var autoCubesOppSwitch: Int = 0,
        var teleCubesFromPortal: Int = 0,
        var teleCubesInExchange: Int = 0,
        var endPosition: EndPosition = EndPosition.NONE,
        var defends: Int = 0
) : Serializable {

    val autoCubesTotal get(): Int = autoCubesOppSwitch + autoCubesOwnSwitch + autoCubesScale

    companion object {
        private const val serialVersionUID: Long = 92387465
    }
}

data class AlliancePerformance(
        val teams: List<TeamPerformance>,
        var points: Int = 0,
        var pwrBoost: Int = 0,
        var pwrLevi: Int = 0,
        var pwrForce: Int = 0,
        var endVaultCubes: Int = 0,
        var vaultCubes: Int = 0,
        var penalties: Int = 0
) {
    companion object {
        fun fromTeams(t1: Int, t2: Int, t3: Int) = AlliancePerformance(listOf(
                TeamPerformance(t1),
                TeamPerformance(t2),
                TeamPerformance(t3)
        ))
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
        var red: AlliancePerformance,
        var blue: AlliancePerformance) : Match(number) {
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
