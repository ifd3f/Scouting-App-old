package com.burlingamerobotics.scouting.activity

import android.os.Bundle
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DialogFragment
import android.app.TimePickerDialog
import android.content.Intent
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import com.burlingamerobotics.scouting.R

import kotlinx.android.synthetic.main.activity_competition_editor.*
import java.util.*

class CompetitionEditorActivity : Activity() {

    lateinit var date: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_competition_editor)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val name = findViewById<EditText>(R.id.edit_name)
        findViewById<Button>(R.id.btn_pick_date).setOnClickListener {
            DatePickerDialog(this, { dp, y, m, d ->
                date = Calendar.getInstance()
                date.set(y, m, d)
            }, 1, 1, 1).show()
        }

        findViewById<Button>(R.id.btn_submit).setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("name", name.text)
                putExtra("date", date)
                finish()
            })
        }
    }

}
