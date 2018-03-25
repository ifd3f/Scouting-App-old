package com.burlingamerobotics.scouting.common.data

import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*


class Competition(
        var name: String,
        cal: Calendar? = null,
        val uuid: UUID = UUID.randomUUID(),
        matchList: List<Match> = listOf(),
        var tbaCode: String? = null
) : Serializable {

    val qualifiers = MatchSchedule(matchList)

    var date: Date = cal?.time ?: Calendar.getInstance().time

    fun getHeader(): CompetitionFileHeader = CompetitionFileHeader(uuid, name, date, qualifiers.count())

    fun getFilename(): String = "$uuid.dat"

    override fun toString(): String {
        return "Competition($uuid: $name)"
    }

    companion object {
        private const val serialVersionUID: Long = 1793484567
    }

}

class CompetitionFileHeader(val uuid: UUID, val name: String, val date: Date, val qualifiers: Int) : Serializable {

    fun getTitle() = "$name (${SimpleDateFormat.getDateInstance().format(date.time)})"
    companion object {
        private const val serialVersionUID: Long = 1723544567
    }
}

data class Team(val number: Int, val name: String) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 7928534234
    }
}
