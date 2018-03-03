package com.burlingamerobotics.scouting.activity

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import com.burlingamerobotics.scouting.R
import com.burlingamerobotics.scouting.ScoutingDB
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.data.PlannedMatch

import kotlinx.android.synthetic.main.activity_competition_selection.*
import java.util.*

class CompetitionSelectionActivity : Activity() {

    lateinit var lvCompetitions: ListView
    lateinit var btnAddCompetition: FloatingActionButton
    lateinit var dbScouting: ScoutingDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_competition_selection)

        Log.i("CompSel", "Competition Selection")

        dbScouting = ScoutingDB(this)
        dbScouting.prepareDirs()

        lvCompetitions = findViewById(R.id.list_competitions)
        btnAddCompetition = findViewById(R.id.btn_add_competition)

        btnAddCompetition.setOnClickListener {
            Log.d("CompSel", "Opening competition creation menu")
            startActivityForResult(Intent(this, CompetitionEditorActivity::class.java), 1)
        }

        refreshCompetitions()
    }

    fun refreshCompetitions() {
        lvCompetitions.adapter = ArrayAdapter(
                this, android.R.layout.simple_list_item_1,
                dbScouting.listCompetitions().map {
                    Log.d("CompSel", "found ${it.uuid}")
                    "${it.name} (${it.uuid})"
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val date = data!!.getSerializableExtra("date") as Date
        val name = data.getStringExtra("name")
        //val comp = Competition(UUID.randomUUID(), name, date, Array(30, {PlannedMatch(it, Array())}))
        // TODO FILL THIS STUFF OUT
    }

}
