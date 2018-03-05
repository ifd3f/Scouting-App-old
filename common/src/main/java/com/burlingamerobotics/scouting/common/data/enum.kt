package com.burlingamerobotics.scouting.common.data

import java.io.Serializable


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

