package com.burlingamerobotics.scouting

import android.content.Context
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.data.CompetitionFileHeader
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.util.*


class ScoutingDB(private val context: Context) {

    fun prepareDirs() {
        dirCompetitions.mkdirs()
    }

    val dirCompetitions = File(context.filesDir, "competitions/")
    val dirTeamData = File(context.filesDir, "teams.json")

    fun listCompetitions(): List<CompetitionFileHeader> {
        return dirCompetitions.listFiles().map {
            ObjectInputStream(BufferedInputStream(FileInputStream(it))).use {
                it.readObject() as CompetitionFileHeader
            }
        }
    }

    fun getCompetition(uuid: UUID): Competition? {
        val file = File(dirCompetitions, "${uuid.toString()}.dat")
        if (!file.exists()) return null

        return ObjectInputStream(BufferedInputStream(FileInputStream(file))).use {
            it.readObject()  // Clear the header
            it.readObject() as Competition
        }
    }

}