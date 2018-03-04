package com.burlingamerobotics.scouting.server.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.data.CompetitionFileHeader
import com.burlingamerobotics.scouting.common.data.MatchTree
import com.burlingamerobotics.scouting.server.R
import com.burlingamerobotics.scouting.server.REQUEST_CODE_CREATE_COMPETITION
import com.burlingamerobotics.scouting.server.ScoutingDB
import com.burlingamerobotics.scouting.server.ScoutingServer
import java.text.SimpleDateFormat
import java.util.*

class CompetitionSelectionActivity : Activity() {

    val TAG = "CompSel"

    lateinit var lvCompetitions: ListView
    lateinit var btnAddCompetition: FloatingActionButton
    lateinit var dbScouting: ScoutingDB
    lateinit var refresher: SwipeRefreshLayout
    lateinit var listComps: List<CompetitionFileHeader>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_competition_selection)

        Log.i(TAG, "Competition Selection")

        dbScouting = ScoutingDB(this)
        dbScouting.prepareDirs()
        ScoutingServer.db = dbScouting

        lvCompetitions = findViewById(R.id.list_competitions)
        btnAddCompetition = findViewById(R.id.btn_add_competition)
        refresher = findViewById(R.id.refresh_competitions)

        btnAddCompetition.setOnClickListener {
            Log.d(TAG, "Starting competition creation activity")
            startActivityForResult(
                    Intent(this, CompetitionEditorActivity::class.java),
                    REQUEST_CODE_CREATE_COMPETITION
            )
        }

        refresher.setOnRefreshListener {
            refreshCompetitions()
        }

        lvCompetitions.setOnItemClickListener { parent, view, position, id ->
            val comp = dbScouting.getCompetition(listComps[position].uuid)
            startActivity(Intent(this, CompetitionInfoActivity::class.java).apply {
                putExtra("competition", comp)
            })
        }

        refreshCompetitions()
    }

    fun refreshCompetitions() {
        Log.i(TAG, "Refreshing competition list")
        listComps = dbScouting.listCompetitions()
        Log.d(TAG, "Found ${listComps.size} competitions")
        lvCompetitions.adapter = ArrayAdapter(
                this, android.R.layout.simple_list_item_1,
                listComps.map { it.getTitle() }
        )
        refresher.isRefreshing = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_CREATE_COMPETITION -> {
                if (resultCode == RESULT_CANCELED) {
                    Log.i(TAG, "User cancelled competition creation")
                    return
                }
                val date = data!!.getSerializableExtra("date") as Calendar
                val name = data.getStringExtra("name")
                val uuid = data.getSerializableExtra("uuid") as UUID

                Log.i(TAG, "Received competition creation data: $name on ${SimpleDateFormat.getDateInstance().format(date.time)} ($uuid)")

                val comp = Competition(uuid, name, date, mutableListOf(), MatchTree.generateTournament(3))
                dbScouting.save(comp)
                refreshCompetitions()
            }
        }
    }

}
