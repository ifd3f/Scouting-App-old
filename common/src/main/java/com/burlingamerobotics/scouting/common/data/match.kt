package com.burlingamerobotics.scouting.common.data

import android.content.res.Resources
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.burlingamerobotics.scouting.common.R
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
) : Serializable {

    val alliance get(): Alliance = Alliance(teams[0].teamNumber, teams[1].teamNumber, teams[2].teamNumber)

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

    val scheduledMatch get(): ScheduledMatch = ScheduledMatch(red.alliance, blue.alliance)

    fun applyTo(view: View, index: Int) {

        view.findViewById<TextView>(R.id.label_match_number).text = index.toString()
        view.findViewById<TextView>(R.id.text_alliance_red_score).text = red.points.toString()
        view.findViewById<TextView>(R.id.text_team_red1).text = red.alliance.a.toString()
        view.findViewById<TextView>(R.id.text_team_red2).text = red.alliance.b.toString()
        view.findViewById<TextView>(R.id.text_team_red3).text = red.alliance.c.toString()
        view.findViewById<TextView>(R.id.text_alliance_blue_score).text = blue.points.toString()
        view.findViewById<TextView>(R.id.text_team_blue1).text = blue.alliance.a.toString()
        view.findViewById<TextView>(R.id.text_team_blue2).text = blue.alliance.b.toString()
        view.findViewById<TextView>(R.id.text_team_blue3).text = blue.alliance.c.toString()

        val (winner, color) = when (matchResult) {
            GameResult.RED_VICTORY -> {
                Pair("Red Victory", R.color.team_red)
            }
            GameResult.BLUE_VICTORY -> {
                Pair("Blue Victory", R.color.team_blue)
            }
            GameResult.DRAW -> {
                Pair("Draw", R.color.team_neutral)
            }
            else -> Pair("Scheduled", R.color.team_neutral)
        }

        val result = view.findViewById<TextView>(R.id.text_match_result)
        result.text = winner
        (result.parent as RelativeLayout).setBackgroundResource(color)
    }

    val hasHappened get() = time != null
    val matchResult get(): GameResult? = when {
        !hasHappened -> null
        red.points > blue.points -> GameResult.RED_VICTORY
        red.points < blue.points -> GameResult.BLUE_VICTORY
        else -> GameResult.DRAW
    }

    override fun toString(): String {
        return "Match(${red.alliance} vs ${blue.alliance})"
    }

    companion object {
        private const val serialVersionUID: Long = 234905687
    }
}