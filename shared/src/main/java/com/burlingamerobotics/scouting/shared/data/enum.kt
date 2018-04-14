package com.burlingamerobotics.scouting.shared.data

import java.io.Serializable


enum class CubePosition : Serializable {
    OWN_SWITCH, SCALE, OPP_SWITCH
}

enum class StartPosition : Serializable {
    EXCHANGE, MIDDLE, PORTAL
}

enum class EndState : Serializable {
    NONE_OR_LEVITATE, PARKED, ASSISTED, CLIMBED_1, CLIMBED_2, CLIMBED_3
}

enum class Rating(val number: Int) : Serializable {
    NONE(0), R_1(1), R_2(2), R_3(3), R_4(4), R_5(5)
}

enum class EndAssistStatus : Serializable {
    NONE_OR_LEVITATE, SELF, ASSISTED, ASSIST2, ASSIST3
}

enum class TeamColor(val victory: GameResult) : Serializable {
    RED(GameResult.RED_VICTORY), BLUE(GameResult.BLUE_VICTORY), NONE(GameResult.DRAW)
}

enum class GameResult : Serializable {
    RED_VICTORY, BLUE_VICTORY, DRAW
}

