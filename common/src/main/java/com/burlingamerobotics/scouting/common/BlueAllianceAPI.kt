package com.burlingamerobotics.scouting.common

import com.burlingamerobotics.scouting.common.data.AlliancePerformance
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.data.Match
import com.burlingamerobotics.scouting.common.data.MatchTree
import khttp.get
import khttp.responses.Response
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern


object BlueAllianceAPI {

    val DATE_REGEX = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})")

    fun fetch(location: String): Response {
        val response = get(URL_TBA_API + location, mapOf("X-TBA-Auth-Key" to API_KEY_TBA))
        response.encoding = Charsets.UTF_8
        return response
    }

    fun fetchCompetition(event: String): Competition? {
        val response = fetch("event/$event")
        return when (response.statusCode) {
            404 -> null
            else -> {
                val data = response.jsonObject
                val name = data.getString("name")
                val cal = run {
                    val match = DATE_REGEX.matcher(data.getString("start_date"))
                    assert(match.matches(), { "Date doesn't match!" })
                    val cal = Calendar.getInstance()
                    cal.set(
                            match.group(1).toInt(),
                            match.group(2).toInt(),
                            match.group(3).toInt())
                    cal
                }

                val matches: List<Match> = run {
                    val json = fetch("event/$event/matches").jsonArray
                    (0 until json.length()).map { i -> getMatchFrom(json.getJSONObject(i))}
                }

                Competition(
                        UUID.randomUUID(),
                        name,
                        cal,
                        matches.toMutableList(),
                        MatchTree.generateTournament(1)
                )
            }
        }
    }

    fun getTeamsFrom(array: JSONArray): List<Int> {
        return (0 until array.length()).map { i -> array.getString(i).drop(3).toInt() }
    }

    fun getMatchFrom(json: JSONObject): Match {
        val alliances = json.getJSONObject("alliances")

        val redJson = alliances.getJSONObject("red").getJSONArray("team_keys")
        val blueJson = alliances.getJSONObject("blue").getJSONArray("team_keys")
        val redSurrJson = alliances.getJSONObject("red").getJSONArray("team_keys")
        val blueSurrJson = alliances.getJSONObject("blue").getJSONArray("team_keys")

        val redTeams = getTeamsFrom(redJson) + getTeamsFrom(redSurrJson)
        val blueTeams = getTeamsFrom(blueJson) + getTeamsFrom(blueSurrJson)

        val ts = json.getLong("actual_time")
        val time = if (ts == 0L) null else Date(ts * 1000)
        return Match(
                json.getInt("match_number"),
                AlliancePerformance.fromTeams(redTeams),
                AlliancePerformance.fromTeams(blueTeams),
                time
        )
    }

}
