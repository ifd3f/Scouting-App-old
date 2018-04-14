package com.burlingamerobotics.scouting.shared.data

import com.burlingamerobotics.scouting.shared.csv.CSVColumn
import com.burlingamerobotics.scouting.shared.csv.CSVSerializer
import com.burlingamerobotics.scouting.shared.csv.createSerializers
import java.io.Serializable
import java.util.*


data class CubeStats(val position: CubePosition, var hit: Int = 0, var miss: Int = 0) : Serializable {
    val accuracy: Double get() = if (miss == 0) {
        if (hit == 0) {
            0.0
        } else {
            1.0
        }
    } else {
        (hit / (hit + miss)).toDouble()
    }
}

data class TeamPerformance(
        var match: Int,
        var team: TeamColor,
        var teamNumber: Int,
        var autoStartPos: StartPosition = StartPosition.MIDDLE,
        var autoCrossedLine: Boolean = false,
        var autoCubePlacement: CubePosition = CubePosition.OWN_SWITCH,
        var autoCubes: Int = 0,
        var autoTimeRemaining: Int = 15,
        var teleCubesOwnSwitch: CubeStats = CubeStats(CubePosition.OWN_SWITCH),
        var teleCubesScale: CubeStats = CubeStats(CubePosition.OWN_SWITCH),
        var teleCubesOppSwitch: CubeStats = CubeStats(CubePosition.OWN_SWITCH),
        var teleCubesExchange: Int = 0,
        var endState: EndState = EndState.NONE_OR_LEVITATE,
        var defends: Int = 0,
        var ratingSwitch: Rating = Rating.NONE,
        var ratingScale: Rating = Rating.NONE,
        var ratingExchange: Rating = Rating.NONE,
        var ratingDefense: Rating = Rating.NONE,
        var ratingIntake: Rating = Rating.NONE
) : Serializable {

    companion object {
        private const val serialVersionUID: Long = 92387465
    }
}

data class AlliancePerformance(
        val teams: Array<TeamPerformance>,
        var points: Int = 0,
        var pwrBoost: Int = 0,
        var pwrLevi: Int = 0,
        var pwrForce: Int = 0,
        var endVaultCubes: Int = 0,
        var vaultCubes: Int = 0,
        var penalties: Int = 0
) : Serializable {

    val alliance = AllianceView(this)

    companion object {
        fun fromTeams(match: Int, teamColor: TeamColor, t1: Int, t2: Int, t3: Int) = AlliancePerformance(arrayOf(
                TeamPerformance(match, teamColor, t1),
                TeamPerformance(match, teamColor, t2),
                TeamPerformance(match, teamColor, t3)
        ))

        fun fromTeams(match: Int, teamColor: TeamColor, seq: List<Int>) = fromTeams(match, teamColor, seq[0], seq[1], seq[2])
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AlliancePerformance

        if (!Arrays.equals(teams, other.teams)) return false
        if (points != other.points) return false
        if (pwrBoost != other.pwrBoost) return false
        if (pwrLevi != other.pwrLevi) return false
        if (pwrForce != other.pwrForce) return false
        if (endVaultCubes != other.endVaultCubes) return false
        if (vaultCubes != other.vaultCubes) return false
        if (penalties != other.penalties) return false
        if (alliance != other.alliance) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(teams)
        result = 31 * result + points
        result = 31 * result + pwrBoost
        result = 31 * result + pwrLevi
        result = 31 * result + pwrForce
        result = 31 * result + endVaultCubes
        result = 31 * result + vaultCubes
        result = 31 * result + penalties
        result = 31 * result + alliance.hashCode()
        return result
    }
}

data class Match(
        var number: Int,
        var red: AlliancePerformance,
        var blue: AlliancePerformance,
        /**
         * When this match happened, or null if it hasn't happened.
         */
        var timeStarted: Date? = null) : Serializable {

    val hasHappened get() = timeStarted != null
    val matchResult get(): GameResult? = when {
        !hasHappened -> null
        red.points > blue.points -> GameResult.RED_VICTORY
        red.points < blue.points -> GameResult.BLUE_VICTORY
        else -> GameResult.DRAW
    }

    override fun toString(): String {
        return "Match(red=${red.alliance}, blue=${blue.alliance})"
    }

    fun getTeamPerformanceOf(team: Int): TeamPerformance? {
        return red.teams.find { it.teamNumber == team } ?: blue.teams.find { it.teamNumber == team }
    }

    fun putTeamPerformance(perf: TeamPerformance) {
        val red = red.teams.indexOfFirst { it.teamNumber == perf.teamNumber }
        val blue = blue.teams.indexOfFirst { it.teamNumber == perf.teamNumber }
        if (red != -1) {
            this.red.teams[red] = perf
        } else if (blue != -1) {
            this.blue.teams[blue] = perf
        }
    }

    companion object {
        private const val serialVersionUID: Long = 234905687

        val CSV_SER = CSVSerializer(
                createSerializers<TeamPerformance>(listOf(
                        "teleCubesOwnSwitch",
                        "teleCubesScale",
                        "teleCubesOppSwitch"
                )) + listOf(
                        CSVColumn("teleCubesOwnSwitch_hit") {
                            it.teleCubesOwnSwitch.hit.toString()
                        },
                        CSVColumn("teleCubesOwnSwitch_miss") {
                            it.teleCubesOwnSwitch.miss.toString()
                        },
                        CSVColumn("teleCubesScale_hit") {
                            it.teleCubesScale.hit.toString()
                        },
                        CSVColumn("teleCubesScale_miss") {
                            it.teleCubesScale.miss.toString()
                        },
                        CSVColumn("teleCubesOppSwitch_hit") {
                            it.teleCubesOppSwitch.hit.toString()
                        },
                        CSVColumn("teleCubesOppSwitch_miss") {
                            it.teleCubesOppSwitch.miss.toString()
                        }
                )
        )

        fun empty(number: Int) = Match(
                number,
                AlliancePerformance.fromTeams(number, TeamColor.RED, 0, 0, 0),
                AlliancePerformance.fromTeams(number, TeamColor.BLUE, 0, 0, 0))

        fun fromTeams(number: Int, red: List<Int>, blue: List<Int>) = Match(
                number,
                AlliancePerformance.fromTeams(number, TeamColor.RED, red),
                AlliancePerformance.fromTeams(number, TeamColor.BLUE, blue)
        )
    }
}