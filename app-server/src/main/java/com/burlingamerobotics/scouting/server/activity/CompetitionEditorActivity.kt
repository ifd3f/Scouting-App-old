package com.burlingamerobotics.scouting.server.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.burlingamerobotics.scouting.server.R
import java.text.SimpleDateFormat
import java.util.*

class CompetitionEditorActivity : Activity() {

    val date: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_competition_editor)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val name = findViewById<EditText>(R.id.edit_name)
        val dateDisplay = findViewById<TextView>(R.id.text_show_date)

        findViewById<Button>(R.id.btn_pick_date).setOnClickListener {
            DatePickerDialog(this, { dp, y, m, d ->
                date.set(y, m, d)
                dateDisplay.text = SimpleDateFormat.getDateInstance().format(date.time)
            }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show()
        }

        val quals = findViewById<EditText>(R.id.edit_qualifiers)

        setResult(Activity.RESULT_CANCELED)

        findViewById<Button>(R.id.btn_submit).setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("name", name.text.toString())
                putExtra("date", date)
                putExtra("qualifiers", quals.text.toString().toInt())
            })
            finish()
        }
    }

}
