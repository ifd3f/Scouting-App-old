package com.burlingamerobotics.scouting.server.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.server.INTENT_EDIT_COMPETITION
import com.burlingamerobotics.scouting.server.R
import java.text.SimpleDateFormat
import java.util.*

class CompetitionEditorActivity : Activity() {

    var date: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_competition_editor)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val editName = findViewById<EditText>(R.id.edit_name)
        val dateDisplay = findViewById<TextView>(R.id.text_show_date)

        var uuid: UUID? = null

        when (intent.action) {
            INTENT_EDIT_COMPETITION -> {
                val oldCompetition = intent.extras["competition"] as Competition
                date = Calendar.getInstance().apply { time = oldCompetition.date }
                uuid = oldCompetition.uuid
                editName.setText(oldCompetition.name)
            }
        }

        findViewById<Button>(R.id.btn_pick_date).setOnClickListener {
            DatePickerDialog(this, { dp, y, m, d ->
                date.set(y, m, d)
                dateDisplay.text = SimpleDateFormat.getDateInstance().format(date.time)
            }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show()
        }

        setResult(Activity.RESULT_CANCELED)

        findViewById<Button>(R.id.btn_submit).setOnClickListener {
            val name = editName.text.toString()
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("name", name)
                putExtra("date", date)
                putExtra("uuid", uuid ?: UUID.randomUUID())
            })
            finish()
        }
    }

}
