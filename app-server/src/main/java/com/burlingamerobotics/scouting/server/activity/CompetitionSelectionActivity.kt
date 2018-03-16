package com.burlingamerobotics.scouting.server.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.burlingamerobotics.scouting.common.data.CompetitionBuilder
import com.burlingamerobotics.scouting.common.data.CompetitionFileHeader
import com.burlingamerobotics.scouting.server.*
import com.burlingamerobotics.scouting.server.dialog.NewCompetitionDialog
import com.burlingamerobotics.scouting.server.io.ScoutingDB
import com.burlingamerobotics.scouting.server.io.ScoutingServer

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
            NewCompetitionDialog(this).show()
        }

        lvCompetitions.setOnItemLongClickListener { _, _, position, _ ->
            startActivityForResult(
                    Intent(this, CompetitionEditorActivity::class.java).apply {
                        putExtra("competition", dbScouting.getCompetition(listComps[position]))
                        putExtra("request", REQUEST_CODE_EDIT_COMPETITION)
                    },
                    REQUEST_CODE_EDIT_COMPETITION
            )
            true
        }

        lvCompetitions.setOnItemClickListener { _, _, position, _ ->
            val comp = dbScouting.getCompetition(listComps[position].uuid)
            startActivity(Intent(this, CompetitionInfoActivity::class.java).apply {
                putExtra("competition", comp)
            })
        }

        refresher.setOnRefreshListener {
            refreshCompetitions()
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
            REQUEST_CODE_NEW_COMPETITION, REQUEST_CODE_EDIT_COMPETITION -> {
                if (resultCode == RESULT_CANCELED) {
                    Log.i(TAG, "User cancelled competition creation")
                    return
                }
                val builder = data!!.getSerializableExtra("builder") as CompetitionBuilder

                Log.i(TAG, "Received competition builder $builder")

                val comp = builder.create()
                dbScouting.save(comp)
                refreshCompetitions()
            }
        }
    }

}
