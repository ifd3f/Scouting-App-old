package com.burlingamerobotics.scouting.common.data

import java.io.Serializable
import java.util.*

class CompetitionBuilder(
        var name: String,
        var finalRounds: Int,
        var uuid: UUID? = null
) : Serializable {

    var qualSchedule = MatchSchedule()
    val calendar: Calendar = Calendar.getInstance()

    fun create(): Competition {
        return Competition(uuid ?: UUID.randomUUID(), name, calendar, qualSchedule.generateMatches(), MatchTree.generateTournament(finalRounds))
    }

    companion object {
        fun from(comp: Competition): CompetitionBuilder {
            return CompetitionBuilder(comp.name, comp.qualifiers.size, comp.uuid).apply {
                qualSchedule = MatchSchedule.from(comp.qualifiers)
            }
        }
    }

}

class MatchSchedule : Serializable {
    val matches: ArrayList<Pair<IntArray, IntArray>> = arrayListOf() /*MutableList(number, {
        Pair(
                IntArray(3, {0}),
                IntArray(3, {0})
        )
    })*/

    fun generateMatches(): MutableList<Match> {
        return matches.mapIndexed { i, (red, blue) ->
            Match(i + 1,
                    AlliancePerformance.fromTeams(red[0], red[1], red[2]),
                    AlliancePerformance.fromTeams(blue[0], blue[1], blue[2]))
        }.toMutableList()
    }

    fun changeSizeTo(newSize: Int) {
        val currentSize = matches.size
        if (newSize < currentSize) {
            for (i in currentSize downTo newSize) {
                matches.removeAt(i - 1)
            }
        } else if (newSize > currentSize) {
            for (i in 1..(newSize - currentSize)) {
                addEmpty()
            }
        }
    }

    fun addEmpty() {
        matches.add(Pair(
                intArrayOf(0, 0, 0),
                intArrayOf(0, 0, 0)
        ))
    }

    companion object {
        fun from(list: List<Match>): MatchSchedule {
            return MatchSchedule().apply {
                list.forEach { match ->
                    matches.add(Pair(match.red.teams.map { it.teamNumber }.toIntArray(), match.blue.teams.map { it.teamNumber }.toIntArray()))
                }
            }
        }
    }
}