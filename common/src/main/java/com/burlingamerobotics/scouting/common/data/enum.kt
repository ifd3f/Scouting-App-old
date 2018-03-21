package com.burlingamerobotics.scouting.common.data

import java.io.Serializable


enum class CubePosition : Serializable {
    OWN_SWITCH, SCALE, OPP_SWITCH
}

enum class StartPosition : Serializable {
    LEFT, CENTER, RIGHT
}

enum class EndPosition : Serializable {
    NONE_OR_LEVITATE, PARKED, CLIMBED
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

