package com.burlingamerobotics.scouting.server

import android.content.Context
import android.util.Log
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.data.CompetitionFileHeader
import com.burlingamerobotics.scouting.common.data.Team
import java.io.*
import java.util.*


class ScoutingDB(context: Context) {

    val TAG = "ScoutingDB-${context.packageName}"

    val dirCompetitions = File(context.filesDir, "competitions/")
    val dirTeamsData = File(context.filesDir, "teams.json")

    private var teams: Lazy<SortedMap<Int, Team>> = lazy {
        if (dirTeamsData.exists()) {
            val teams = Klaxon().parse<JsonArray<Team>>(dirTeamsData)
            teams!!.map { it.number to it }.toMap().toSortedMap()
        } else {
            sortedMapOf()
        }
    }

    private var teamsChanged = false

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

    fun putTeam(team: Team) {
        teams.value[team.number] = team
        teamsChanged = true
    }

    fun commitTeams() {
        if (teamsChanged) {
            val write = JsonArray(listTeams())
            val json = Klaxon().toJsonString(write)
            FileWriter(dirTeamsData).use {
                it.write(json)
            }
            teamsChanged = false
        }
    }

    fun getTeam(number: Int): Team? {
        return teams.value[number]
    }

    fun listTeams(): List<Team> {
        return teams.value.toList().map { it.second }
    }

}