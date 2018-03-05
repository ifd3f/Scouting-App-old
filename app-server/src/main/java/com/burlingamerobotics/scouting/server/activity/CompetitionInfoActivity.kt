package com.burlingamerobotics.scouting.server.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.server.R
import kotlinx.android.synthetic.main.activity_competition_info.*
import java.text.SimpleDateFormat

class CompetitionInfoActivity : AppCompatActivity() {

    lateinit var competition: Competition

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_competition_info)
        setSupportActionBar(toolbar)

        competition = intent.getSerializableExtra("competition") as Competition

        fab.setOnClickListener { view ->
            startActivity(Intent(this, ServerManagerActivity::class.java).apply {
                putExtra("competition", competition)
            })
        }

        val txtDate: TextView = findViewById(R.id.text_show_date)

        txtDate.text = SimpleDateFormat.getInstance().format(competition.date.time)
        toolbar.title = competition.name

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
