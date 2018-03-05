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

enum class TeamColor(val victory: GameResult) : Serializable {
    RED(GameResult.RED_VICTORY), BLUE(GameResult.BLUE_VICTORY)
}

enum class GameResult : Serializable {
    RED_VICTORY, BLUE_VICTORY, DRAW
}

class MatchSchedule(number: Int) : Serializable {
    val matches: Array<Pair<Array<Int>, Array<Int>>> = Array(number, {
        Pair(
                Array(6, {0}),
                Array(6, {0})
        )
    })

    fun generateMatches(): List<Match> {
        return matches.mapIndexed { i, (red, blue) ->
            Match(i + 1,
                    AlliancePerformance.fromTeams(red[0], red[1], red[2]),
                    AlliancePerformance.fromTeams(blue[0], blue[1], blue[2]))
        }.toList()
    }
}

data class TeamPerformance(
        var teamNumber: Int,
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

class Match(
        val number: Int,
        var red: AlliancePerformance,
        var blue: AlliancePerformance,
        /**
         * When this match happened, or null if it hasn't happened.
         */
        var time: Date? = null) : Serializable {

    val hasHappened get() = time != null
    val winner get(): GameResult? = when {
        !hasHappened -> null
        red.points > blue.points -> GameResult.RED_VICTORY
        red.points < blue.points -> GameResult.BLUE_VICTORY
        else -> GameResult.DRAW
    }

    companion object {
        private const val serialVersionUID: Long = 234905687
    }
}

class Competition(
        val uuid: UUID,
        val name: String,
        cal: Calendar,
        qualifiers: Int,
        val finals: MatchTree
) : Serializable {

    var qualifierSchedule = MatchSchedule(qualifiers)

    var qualifiers: MutableList<Match?>? = null

    val date: Date = cal.time

    fun getHeader(): CompetitionFileHeader = CompetitionFileHeader(uuid, name, date, qualifierSchedule.matches.size)

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
