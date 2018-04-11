package com.burlingamerobotics.scouting.common.data

import java.io.BufferedWriter
import java.io.Serializable

class MatchSchedule(val matches: MutableList<Match> = mutableListOf()) : Serializable, MutableList<Match> by matches {

    fun changeSizeTo(newSize: Int) {
        val currentSize = size
        if (newSize < currentSize) {
            for (i in (currentSize - 1) downTo newSize) {
                removeAt(i)
            }
        } else if (newSize > currentSize) {
            for (i in 1..(newSize - currentSize)) {
                addEmpty()
            }
        }
    }

    fun addEmpty() {
        add(Match.empty(size))
    }

    fun writeCSV(writer: BufferedWriter) {
        //writer.write()
    }

    companion object {
        fun from(list: List<Match>): MatchSchedule {
            return MatchSchedule().apply {
                list.forEach { matches.add(it) }
            }
        }
    }
}

interface Alliance : Serializable, Iterable<Int> {
    var a: Int
    var b: Int
    var c: Int

    fun component1(): Int = a
    fun component2(): Int = b
    fun component3(): Int = c

    override fun iterator() = listOf(a, b, c).iterator()

    val strings get() = Triple(
            if (a < 1) "" else a.toString(),
            if (b < 1) "" else b.toString(),
            if (c < 1) "" else c.toString())

}

data class SimpleAlliance(override var a: Int, override var b: Int, override var c: Int) : Alliance {
    constructor() : this(0, 0, 0)

    override fun iterator(): Iterator<Int> {
        return arrayOf(a, b, c).iterator()
    }

    operator fun get(i: Int) = when (i) {
        0 -> a
        1 -> b
        2 -> c
        else -> throw IllegalArgumentException()
    }

    override fun toString(): String = "Alliance($a, $b, $c)"

}

class AllianceView(private val ap: AlliancePerformance) : Alliance {

    override var a: Int
        get() = ap.teams[0].teamNumber
        set(v) {
            ap.teams[0].teamNumber = v
        }
    override var b: Int
        get() = ap.teams[1].teamNumber
        set(v) {
            ap.teams[1].teamNumber = v
        }
    override var c: Int
        get() = ap.teams[2].teamNumber
        set(v) {
            ap.teams[2].teamNumber = v
        }

}
