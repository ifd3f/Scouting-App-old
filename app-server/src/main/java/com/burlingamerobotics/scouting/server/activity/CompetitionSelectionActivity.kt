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

        refreshCompetitions()
    }

    fun refreshCompetitions() {
        Log.i(TAG, "Refreshing competition list")
        listComps = dbScouting.listCompetitions()
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
                val quals = data.getIntExtra("qualifiers", -1)
                assert(quals > 0, { "Error! No qualifiers field found!" })

                Log.i(TAG, "Received competition creation data: $name on $date has $quals qualifier matches")

                val comp = Competition(UUID.randomUUID(), name, date, Array(30, { null }), MatchTree.generateTournament(3))
                dbScouting.save(comp)
                refreshCompetitions()
            }
        }
    }

}
