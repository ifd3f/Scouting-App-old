package com.burlingamerobotics.scouting.shared

import com.burlingamerobotics.scouting.shared.data.Competition
import com.burlingamerobotics.scouting.shared.data.Match
import khttp.get
import khttp.responses.Response
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern


object BlueAllianceAPI {

    val DATE_REGEX = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})")
    val TAG = "BlueAllianceAPI"

    fun fetch(location: String): Response {
        val url = URL_TBA_API + location
        val response = get(url, mapOf("X-TBA-Auth-Key" to API_KEY_TBA))
        response.encoding = Charsets.UTF_8
        return response
    }

    fun fetchCompetition(event: String): com.burlingamerobotics.scouting.shared.data.Competition? {
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

                val matches = run {
                    val matchesResponse = fetch("event/$event/matches")
                    val json = matchesResponse.jsonArray
                    (0 until json.length())
                            .map { i -> json.getJSONObject(i) }
                            .filter { it.getString("comp_level") == "qm" }
                            .map { getMatchFrom(it) }
                }

                Competition(name, cal, UUID.randomUUID(), matches)
            }
        }
    }

    fun getTeamsFrom(array: JSONArray): List<Int> {
        return (0 until array.length()).map { i -> array.getString(i).drop(3).toInt() }
    }

    fun getMatchFrom(json: JSONObject): com.burlingamerobotics.scouting.shared.data.Match {
        val alliances = json.getJSONObject("alliances")

        val redJson = alliances.getJSONObject("red").getJSONArray("team_keys")
        val blueJson = alliances.getJSONObject("blue").getJSONArray("team_keys")
        val redSurrJson = alliances.getJSONObject("red").getJSONArray("surrogate_team_keys")
        val blueSurrJson = alliances.getJSONObject("blue").getJSONArray("surrogate_team_keys")
        val redDQJson = alliances.getJSONObject("red").getJSONArray("dq_team_keys")
        val blueDQJson = alliances.getJSONObject("blue").getJSONArray("dq_team_keys")

        val redTeams = getTeamsFrom(redJson) + getTeamsFrom(redSurrJson) + getTeamsFrom(redDQJson)
        val blueTeams = getTeamsFrom(blueJson) + getTeamsFrom(blueSurrJson) + getTeamsFrom(blueDQJson)

        val number = json.getInt("match_number")
        val ts = json.getLong("actual_time")
        val time = if (ts == 0L) null else Date(ts * 1000)
        return Match.fromTeams(number, redTeams, blueTeams).also {
            it.timeStarted = time
        }
    }

}
