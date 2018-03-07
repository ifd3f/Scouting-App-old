package com.burlingamerobotics.scouting.common.data

import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.burlingamerobotics.scouting.common.R
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
    val matches: MutableList<ScheduledMatch> = arrayListOf() /*MutableList(number, {
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
            for (i in (currentSize - 1) downTo newSize) {
                matches.removeAt(i)
            }
        } else if (newSize > currentSize) {
            for (i in 1..(newSize - currentSize)) {
                addEmpty()
            }
        }
    }

    fun addEmpty() {
        matches.add(ScheduledMatch())
    }

    companion object {
        fun from(list: List<Match>): MatchSchedule {
            return MatchSchedule().apply {
                list.forEach { matches.add(it.scheduledMatch) }
            }
        }
    }
}

data class Alliance(var a: Int, var b: Int, var c: Int) : Serializable {

    constructor() : this(0, 0, 0)
    constructor(teams: List<Int>) : this(teams[0], teams[1], teams[2])

    val strings get() = Triple(
            if (a < 1) "" else a.toString(),
            if (b < 1) "" else b.toString(),
            if (c < 1) "" else c.toString())

    operator fun get(i: Int) = when (i) {
        0 -> a
        1 -> b
        2 -> c
        else -> throw IllegalArgumentException()
    }

    override fun toString(): String = "Alliance($a, $b, $c)"

}

data class ScheduledMatch(val red: Alliance, val blue: Alliance) : Serializable {

    constructor() : this(Alliance(), Alliance())

    fun applyTo(view: View, index: Int) {
        val (r1, r2, r3) = red.strings
        val (b1, b2, b3) = blue.strings
        view.findViewById<TextView>(R.id.label_match_number).text = index.toString()
        view.findViewById<EditText>(R.id.edit_team_red1).setText(r1)
        view.findViewById<EditText>(R.id.edit_team_red2).setText(r2)
        view.findViewById<EditText>(R.id.edit_team_red3).setText(r3)
        view.findViewById<EditText>(R.id.edit_team_blue1).setText(b1)
        view.findViewById<EditText>(R.id.edit_team_blue2).setText(b2)
        view.findViewById<EditText>(R.id.edit_team_blue3).setText(b3)
    }

}
