package com.burlingamerobotics.scouting.server.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.server.*
import java.text.SimpleDateFormat
import java.util.*

class CompetitionEditorActivity : Activity() {

    lateinit var date: Calendar
    lateinit var dateDisplay: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_competition_editor)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val editName = findViewById<EditText>(R.id.edit_name)
        dateDisplay = findViewById<TextView>(R.id.text_show_date)

        val uuid: UUID

        when (intent.getIntExtra("request", -3524768)) {
            REQUEST_CODE_NEW_COMPETITION -> {
                date = Calendar.getInstance()
                uuid = UUID.randomUUID()
            }
            REQUEST_CODE_EDIT_COMPETITION -> {
                val oldCompetition = intent.getSerializableExtra("competition") as Competition
                date = Calendar.getInstance().apply { time = oldCompetition.date }
                uuid = oldCompetition.uuid
                editName.setText(oldCompetition.name)
            }
            else -> throw IllegalStateException("Invalid action ${intent.action}!!")
        }
        updateDate()

        findViewById<Button>(R.id.btn_pick_date).setOnClickListener {
            DatePickerDialog(this, { dp, y, m, d ->
                date.set(y, m, d)
                updateDate()
            }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show()
        }

        setResult(Activity.RESULT_CANCELED)

        findViewById<Button>(R.id.btn_submit).setOnClickListener {
            val name = editName.text.toString()
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("name", name)
                putExtra("date", date)
                putExtra("uuid", uuid)
            })
            finish()
        }
    }

    fun updateDate() {
        dateDisplay.text = SimpleDateFormat.getDateInstance().format(date.time)
    }

}
