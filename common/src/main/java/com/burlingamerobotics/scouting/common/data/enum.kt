package com.burlingamerobotics.scouting.common.data

import com.burlingamerobotics.scouting.common.R
import java.io.Serializable


enum class CubePosition(val resId: Int) : Serializable {
    OWN_SWITCH(R.string.cube_pos_own_switch),
    SCALE(R.string.cube_pos_scale),
    OPP_SWITCH(R.string.cube_pos_opp_switch)
}

enum class StartPosition(val resId: Int) : Serializable {
    EXCHANGE(R.string.start_exchange),
    MIDDLE(R.string.start_middle),
    PORTAL(R.string.start_portal)
}

enum class EndState(val resId: Int) : Serializable {
    NONE_OR_LEVITATE(R.string.endgame_state_none_lev),
    PARKED(R.string.endgame_state_parked),
    ASSISTED(R.string.endgame_state_assisted),
    CLIMBED_1(R.string.endgame_state_climbed_1),
    CLIMBED_2(R.string.endgame_state_climbed_2),
    CLIMBED_3(R.string.endgame_state_climbed_3),
}

enum class Rating(val number: Int, val resId: Int) : Serializable {
    NONE(0, R.string.rating_0),
    R_1(1, R.string.rating_1),
    R_2(2, R.string.rating_2),
    R_3(3, R.string.rating_3),
    R_4(4, R.string.rating_4),
    R_5(5, R.string.rating_5),
}

enum class EndAssistStatus : Serializable {
    NONE_OR_LEVITATE, SELF, ASSISTED, ASSIST2, ASSIST3
}

enum class TeamColor(val victory: GameResult) : Serializable {
    RED(GameResult.RED_VICTORY), BLUE(GameResult.BLUE_VICTORY)
}

enum class GameResult : Serializable {
    RED_VICTORY, BLUE_VICTORY, DRAW
}

