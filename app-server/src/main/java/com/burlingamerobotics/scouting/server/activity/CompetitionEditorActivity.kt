package com.burlingamerobotics.scouting.server.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.data.CompetitionBuilder
import com.burlingamerobotics.scouting.server.*
import java.text.SimpleDateFormat
import java.util.*

class CompetitionEditorActivity : Activity() {

    lateinit var btnDatePicker: TextView
    lateinit var builder: CompetitionBuilder
    var baseComp: Competition? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_competition_editor)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        val editName = findViewById<EditText>(R.id.edit_name)
        btnDatePicker = findViewById<Button>(R.id.btn_pick_date)


        when (intent.getIntExtra("request", -3524768)) {
            REQUEST_CODE_NEW_COMPETITION -> {
                builder = CompetitionBuilder("", 3, UUID.randomUUID())
            }
            REQUEST_CODE_EDIT_COMPETITION -> {
                val comp = intent.getSerializableExtra("competition") as Competition
                builder = CompetitionBuilder.from(comp)
                baseComp = comp
            }
            else -> throw IllegalStateException("Invalid action ${intent.action}!!")
        }
        updateDate()

        btnDatePicker.setOnClickListener {
            DatePickerDialog(this, { dp, y, m, d ->
                builder.calendar.set(y, m, d)
                updateDate()
            }, builder.calendar.get(Calendar.YEAR), builder.calendar.get(Calendar.MONTH), builder.calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        setResult(Activity.RESULT_CANCELED)

        findViewById<Button>(R.id.btn_submit).setOnClickListener {
            val name = editName.text.toString()
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("name", builder)
            })
            finish()
        }
    }

    fun updateDate() {
        btnDatePicker.text = SimpleDateFormat.getDateInstance().format(builder.calendar.time)
    }

}
