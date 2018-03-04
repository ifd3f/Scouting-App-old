package com.burlingamerobotics.scouting.server

import android.content.Context
import android.util.Log
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.data.CompetitionFileHeader
import java.io.*
import java.util.*


class ScoutingDB(private val context: Context) {

    val TAG = "ScoutingDB-${context.packageName}"

    val dirCompetitions = File(context.filesDir, "competitions/")
    val dirTeamData = File(context.filesDir, "teams.json")

    fun prepareDirs() {
        dirCompetitions.mkdirs()
    }

    fun listCompetitions(): List<CompetitionFileHeader> {
        Log.d(TAG, "listing contents of ${dirCompetitions.absolutePath}")
        return dirCompetitions.listFiles().map {
            ObjectInputStream(BufferedInputStream(FileInputStream(it))).use {
                it.readObject() as CompetitionFileHeader
            }
        }
    }

    fun getCompetition(uuid: UUID): Competition? {
        val file = File(dirCompetitions, "$uuid.dat")
        if (!file.exists()) {
            Log.d(TAG, "no competition found for UUID $uuid")
            return null
        }

        return ObjectInputStream(BufferedInputStream(FileInputStream(file))).use {
            it.readObject()  // Clear the header
            it.readObject() as Competition
        }
    }

    fun save(comp: Competition) {
        val file = File(dirCompetitions, comp.getFilename())
        Log.d(TAG, "writing competition data to ${file.absolutePath}")
        ObjectOutputStream(FileOutputStream(file, false)).use {
            it.writeObject(comp.getHeader())
            it.writeObject(comp)
        }
    }

}