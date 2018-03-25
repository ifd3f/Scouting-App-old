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
import com.burlingamerobotics.scouting.common.REQUEST_CODE_EDIT
import com.burlingamerobotics.scouting.common.REQUEST_CODE_NEW
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.server.R
import java.text.SimpleDateFormat
import java.util.*

class CompetitionEditorActivity : Activity() {

    private val TAG = "CompEditorActivity"
    private val sdf = SimpleDateFormat.getDateInstance()

    private lateinit var btnDatePicker: TextView
    private lateinit var comp: Competition
    private lateinit var editRowCount: EditText
    private lateinit var editName: EditText
    private lateinit var lsMatches: LinearLayout

    private val calendar: Calendar = Calendar.getInstance()

    val rows: MutableList<View> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_competition_editor)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        when (intent.getIntExtra("request", -3524768)) {
            REQUEST_CODE_NEW -> {
                Log.i(TAG, "Activity was created to make new competition")
                comp = Competition("", Calendar.getInstance())
                comp.qualifiers.addEmpty()
            }
            REQUEST_CODE_EDIT -> {
                val comp = intent.getSerializableExtra("competition")
                this.comp = when (comp) {
                    is Competition -> {
                        comp
                    }
                    else -> throw IllegalArgumentException("Received something other than Competition!")
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

        editName.setText(comp.name)

        btnDatePicker.setOnClickListener {
            Log.i(TAG, "Spawning DatePicker")
            DatePickerDialog(this, { dp, y, m, d ->
                calendar.set(y, m, d)
                Log.d(TAG, "Date has been set: $y-$m-$d")
                updateDate()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        findViewById<Button>(R.id.btn_set_rows).setOnClickListener {
            val countNew = editRowCount.text.toString().toInt()
            Log.d(TAG, "User selected set rows")
            updateRowsWithRowCount(countNew)
        }

        findViewById<Button>(R.id.btn_add_row).setOnClickListener {
            Log.i(TAG, "Adding a row")
            comp.qualifiers.addEmpty()
            updateRows()
        }

        findViewById<Button>(R.id.btn_submit).setOnClickListener {
            Log.i(TAG, "Submitting data to parent")
            val name = editName.text.toString()
            comp.name = name
            rows.zip(comp.qualifiers).map { (v, m) ->
                m.red.alliance.a = v.findViewById<TextView>(R.id.edit_team_red1).text.toString().toInt()
                m.red.alliance.b = v.findViewById<TextView>(R.id.edit_team_red2).text.toString().toInt()
                m.red.alliance.c = v.findViewById<TextView>(R.id.edit_team_red3).text.toString().toInt()
                m.blue.alliance.a = v.findViewById<TextView>(R.id.edit_team_blue1).text.toString().toInt()
                m.blue.alliance.b = v.findViewById<TextView>(R.id.edit_team_blue2).text.toString().toInt()
                m.blue.alliance.c = v.findViewById<TextView>(R.id.edit_team_blue3).text.toString().toInt()
            }
            comp.date = calendar.time
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra("comp", comp)
            })
            finish()
        }

        updateDate()
        updateRows()
    }

    fun updateDate() {
        val text = sdf.format(comp.date)
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
        comp.qualifiers.changeSizeTo(count)
        updateRows()
    }

    fun updateRows() {
        val matches = comp.qualifiers.matches
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
