package com.burlingamerobotics.scouting.server.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.burlingamerobotics.scouting.common.REQUEST_CODE_EDIT_COMPETITION
import com.burlingamerobotics.scouting.common.REQUEST_CODE_NEW_COMPETITION
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.data.CompetitionBuilder
import com.burlingamerobotics.scouting.server.R
import java.text.SimpleDateFormat
import java.util.*

class CompetitionEditorActivity : Activity() {

    val TAG = "CompEditorActivity"
    val sdf = SimpleDateFormat.getDateInstance()

    lateinit var btnDatePicker: TextView
    lateinit var builder: CompetitionBuilder
    lateinit var editRowCount: EditText
    lateinit var editName: EditText
    lateinit var lsMatches: LinearLayout
    lateinit var vBase: View

    var baseComp: Competition? = null
    val rows: MutableList<View> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_competition_editor)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        when (intent.getIntExtra("request", -3524768)) {
            REQUEST_CODE_NEW_COMPETITION -> {
                Log.i(TAG, "Activity was created to make new competition")
                builder = CompetitionBuilder("", 3, UUID.randomUUID())
                builder.qualSchedule.addEmpty()
            }
            REQUEST_CODE_EDIT_COMPETITION -> {
                val comp = intent.getSerializableExtra("competition")
                builder = when (comp) {
                    is Competition -> {
                        baseComp = comp
                        CompetitionBuilder.from(comp)
                    }
                    is CompetitionBuilder -> {
                        comp
                    }
                    else -> throw IllegalArgumentException("Received something other than Competition or CompetitionBuilder!")
                }
                Log.i(TAG, "Activity was created to edit existing competition")
            }
            else -> throw IllegalStateException("Invalid action ${intent.action}!!")
        }
        setResult(Activity.RESULT_CANCELED)

        editName = findViewById<EditText>(R.id.edit_name)
        btnDatePicker = findViewById<Button>(R.id.btn_pick_date)
        lsMatches = findViewById(R.id.list_matches)
        editRowCount = findViewById(R.id.edit_rows)

        editName.setText(builder.name)

        btnDatePicker.setOnClickListener {
            Log.i(TAG, "Spawning DatePicker")
            DatePickerDialog(this, { dp, y, m, d ->
                builder.calendar.set(y, m, d)
                Log.d(TAG, "Date has been set: $y-$m-$d")
                updateDate()
            }, builder.calendar.get(Calendar.YEAR), builder.calendar.get(Calendar.MONTH), builder.calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        findViewById<Button>(R.id.btn_set_rows).setOnClickListener {
            val countNew = editRowCount.text.toString().toInt()
            Log.d(TAG, "User selected set rows")
            updateRowsWithRowCount(countNew)
        }

        findViewById<Button>(R.id.btn_add_row).setOnClickListener {
            Log.i(TAG, "Adding a row")
            builder.qualSchedule.addEmpty()
            updateRows()
        }

        findViewById<Button>(R.id.btn_submit).setOnClickListener {
            Log.i(TAG, "Submitting data to parent")
            val name = editName.text.toString()
            builder.name = name
            rows.zip(builder.qualSchedule.matches).map { (v, m) ->
                m.red.a = v.findViewById<TextView>(R.id.edit_team_red1).text.toString().toInt()
                m.red.b = v.findViewById<TextView>(R.id.edit_team_red2).text.toString().toInt()
                m.red.c = v.findViewById<TextView>(R.id.edit_team_red3).text.toString().toInt()
                m.blue.a = v.findViewById<TextView>(R.id.edit_team_blue1).text.toString().toInt()
                m.blue.b = v.findViewById<TextView>(R.id.edit_team_blue2).text.toString().toInt()
                m.blue.c = v.findViewById<TextView>(R.id.edit_team_blue3).text.toString().toInt()
            }
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("builder", builder)
            })
            finish()
        }

        updateDate()
        updateRows()
    }

    fun updateDate() {
        val text = sdf.format(builder.calendar.time)
        Log.d(TAG, "Updating date to $text")
        btnDatePicker.text = text
    }

    fun updateRowsWithRowCount(count: Int) {
        if (count < 1) {
            Log.w(TAG, "Attempting to change row count to $count, will not allow")
            updateRows()
            return
        }
        Log.d(TAG, "Updating to $count rows")
        builder.qualSchedule.changeSizeTo(count)
        updateRows()
    }

    fun updateRows() {
        val matches = builder.qualSchedule.matches
        val countMatches = matches.size
        val countList = lsMatches.childCount
        Log.d(TAG, "Updating rows from $countList to $countMatches")
        if (countMatches > countList) {
            Log.d(TAG, "  Creating rows to meet number")
            for (i in countList until countMatches) {
                val view = layoutInflater.inflate(R.layout.item_edit_match_row, lsMatches, false)
                matches[i].applyTo(view, i + 1)
                lsMatches.addView(view)
                rows.add(view)
            }
        } else if (countMatches < countList) {
            val count = countList - countMatches
            Log.d(TAG, "  Deleting $count rows to meet number")
            currentFocus?.clearFocus()
            for (i in (countList - 1) downTo countMatches) {
                Log.d(TAG, "  Removing at $i")
                rows.removeAt(i)
                lsMatches.removeViewAt(i)
            }
        } else {
            Log.d(TAG, "  There is no need to change the number of rows")
        }
        editRowCount.setText(countMatches.toString())
    }

}
