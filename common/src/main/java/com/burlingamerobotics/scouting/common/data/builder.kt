package com.burlingamerobotics.scouting.common.data

import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.burlingamerobotics.scouting.common.R
import java.io.Serializable
import java.util.*

class MatchSchedule(matches: List<Match> = listOf()) : Serializable, MutableIterable<Match> {

    val matches: MutableList<Match> = matches.toMutableList()

    fun generateMatches(): MutableList<Match> {
        return matches.mapIndexed { i, match ->
            val red = match.red.alliance
            val blue = match.blue.alliance
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
        matches.add(Match.empty(matches.size))
    }

    override fun iterator(): MutableIterator<Match> = matches.iterator()

    companion object {
        fun from(list: List<Match>): MatchSchedule {
            return MatchSchedule().apply {
                list.forEach { matches.add(it) }
            }
        }
    }
}

data class Alliance(var a: Int, var b: Int, var c: Int) : Serializable, Iterable<Int> {
    override fun iterator(): Iterator<Int> {
        return arrayOf(a, b, c).iterator()
    }

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

