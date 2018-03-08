package com.burlingamerobotics.scouting.server.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.view.MatchRecyclerViewAdapter
import com.burlingamerobotics.scouting.server.R
import kotlinx.android.synthetic.main.activity_competition_editor.view.*
import kotlinx.android.synthetic.main.activity_competition_info.*
import java.text.SimpleDateFormat

class CompetitionInfoActivity : AppCompatActivity() {

    val TAG = "CompInfoActivity"
    lateinit var competition: Competition
    lateinit var matchList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_competition_info)
        setSupportActionBar(toolbar)

        competition = intent.getSerializableExtra("competition") as Competition
        matchList = findViewById(R.id.list_matches)

        fab.setOnClickListener { view ->
            startActivity(Intent(this, ServerManagerActivity::class.java).apply {
                putExtra("competition", competition)
            })
        }

        matchList.layoutManager = LinearLayoutManager(this)
        matchList.adapter = MatchRecyclerViewAdapter(competition.qualifiers)

        /*
        competition.qualifiers.forEachIndexed { i, match ->
            Log.d(TAG, "Adding view for match #$i: $match")
            val view = layoutInflater.inflate(R.layout.item_show_match_row, matchList, false)
            match.applyTo(view, i)
            matchList.addView(view)
        }*/

        val txtDate: TextView = findViewById(R.id.text_show_date)

        txtDate.text = SimpleDateFormat.getInstance().format(competition.date.time)
        supportActionBar?.title = competition.name

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
