package com.burlingamerobotics.scouting.common

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.burlingamerobotics.scouting.shared.data.*

fun Match.applyTo(view: View, index: Int) {

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

val CubePosition.resId get(): Int = when (this) {
    CubePosition.OWN_SWITCH -> R.string.cube_pos_own_switch
    CubePosition.SCALE -> R.string.cube_pos_scale
    CubePosition.OPP_SWITCH -> R.string.cube_pos_opp_switch
}

val StartPosition.resId get(): Int = when (this) {
    StartPosition.EXCHANGE -> R.string.start_exchange
    StartPosition.MIDDLE -> R.string.start_middle
    StartPosition.PORTAL -> R.string.start_portal
}

val EndState.resId get(): Int = when (this) {
    EndState.NONE_OR_LEVITATE -> R.string.endgame_state_none_lev
    EndState.PARKED -> R.string.endgame_state_parked
    EndState.ASSISTED -> R.string.endgame_state_assisted
    EndState.CLIMBED_1 -> R.string.endgame_state_climbed_1
    EndState.CLIMBED_2 -> R.string.endgame_state_climbed_2
    EndState.CLIMBED_3 -> R.string.endgame_state_climbed_3
}

val Rating.resId get(): Int = when (this) {
    Rating.NONE -> R.string.rating_0
    Rating.R_1 -> R.string.rating_1
    Rating.R_2 -> R.string.rating_2
    Rating.R_3 -> R.string.rating_3
    Rating.R_4 -> R.string.rating_4
    Rating.R_5 -> R.string.rating_5
}
