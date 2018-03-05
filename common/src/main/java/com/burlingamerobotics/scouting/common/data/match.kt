package com.burlingamerobotics.scouting.common.data

import java.io.Serializable
import java.util.*


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