package com.burlingamerobotics.scouting.common.data

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*


class Competition(
        val uuid: UUID,
        val name: String,
        cal: Calendar,
        val qualifiers: MutableList<Match>,
        val finals: MatchTree
) : Serializable {

    val date: Date = cal.time

    fun getHeader(): CompetitionFileHeader = CompetitionFileHeader(uuid, name, date, qualifiers.size ?: 0)

    fun getFilename(): String = "$uuid.dat"

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
