package com.burlingamerobotics.scouting.server.io

import android.content.Context
import android.util.Log
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.burlingamerobotics.scouting.shared.data.Competition
import com.burlingamerobotics.scouting.shared.data.CompetitionFileHeader
import com.burlingamerobotics.scouting.shared.data.Team
import java.io.*
import java.util.*


class ScoutingDB(context: Context) {

    val TAG = "ScoutingDB-${context.packageName}"

    val dirCompetitions = File(context.filesDir, "competitions/")
    val dirTeamsData = File(context.filesDir, "teams.json")

    private val teams: Lazy<SortedMap<Int, Team>> = lazy {
        Log.d(TAG, "Generating teams map")
        val klaxon = Klaxon()
        if (dirTeamsData.exists()) {
            Log.d(TAG, "${dirTeamsData.absolutePath} exists, reading from it")
            val teams = mutableListOf<Team>()
            JsonReader(FileReader(dirTeamsData)).use {reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        teams.add(klaxon.parse<Team>(reader)!!)
                    }
                }
            }
            Log.d(TAG, "Collected $teams")
            val out = teams.map { it.number to it }.toMap().toSortedMap()
            out
        } else {
            Log.w(TAG, "${dirTeamsData.absolutePath} does not exist, will use empty one")
            sortedMapOf()
        }
    }

    private var teamsChanged = false
    private var lockTeamWrite = Object()
    private var lockCompetitionWrite = Object()

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

    fun getCompetition(header: CompetitionFileHeader): Competition? {
        return getCompetition(header.uuid)
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
        synchronized(lockCompetitionWrite) {
            val file = File(dirCompetitions, comp.getFilename())
            Log.d(TAG, "writing competition data to ${file.absolutePath}")
            ObjectOutputStream(FileOutputStream(file, false)).use {
                it.writeObject(comp.getHeader())
                it.writeObject(comp)
            }
        }
    }

    fun putTeam(team: Team) {
        teams.value[team.number] = team
        teamsChanged = true
    }

    fun commitTeams() {
        synchronized(lockTeamWrite) {
            if (teamsChanged) {
                Log.d(TAG, "teams was changed, writing to disk")
                val write = JsonArray(listTeams())
                val json = Klaxon().toJsonString(write)
                FileWriter(dirTeamsData).use {
                    it.write(json)
                }
                teamsChanged = false
            } else {
                Log.d(TAG, "teams was not changed, will not write data to disk")
            }
        }
    }

    fun getTeam(number: Int): Team? {
        return teams.value[number]
    }

    fun listTeams(): List<Team> {
        return teams.value.toList().map { it.second }
    }

}