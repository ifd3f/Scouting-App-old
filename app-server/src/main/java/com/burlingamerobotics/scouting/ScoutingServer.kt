package com.burlingamerobotics.scouting

import com.burlingamerobotics.scouting.common.data.Competition

/**
 * Manages all the client threads
 */
object ScoutingServer {

    val clients: MutableList<ClientResponseThread> = mutableListOf()

    lateinit var competition: Competition

    fun start(competition: Competition) {
        this.competition = competition
    }

}