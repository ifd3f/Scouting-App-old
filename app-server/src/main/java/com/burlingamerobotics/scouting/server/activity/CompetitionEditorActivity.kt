package com.burlingamerobotics.scouting.server.activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.burlingamerobotics.scouting.common.REQUEST_CODE_EDIT
import com.burlingamerobotics.scouting.common.REQUEST_CODE_NEW
import com.burlingamerobotics.scouting.server.R
import com.burlingamerobotics.scouting.shared.data.Competition
import com.burlingamerobotics.scouting.shared.data.Match
import com.burlingamerobotics.scouting.shared.data.MatchSchedule
import kotlinx.android.synthetic.main.activity_competition_editor.*
import java.text.SimpleDateFormat
import java.util.*

class CompetitionEditorActivity : Activity() {

    private val TAG = "CompEditorActivity"
    private val sdf = SimpleDateFormat.getDateInstance()

    private lateinit var comp: Competition

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


        list_edit_matches.layoutManager = LinearLayoutManager(this)
        list_edit_matches.adapter = EditMatchListRecyclerAdapter(comp.qualifiers)

        edit_name.setText(comp.name)

        btn_pick_date.setOnClickListener {
            Log.i(TAG, "Spawning DatePicker")
            DatePickerDialog(this, { _, y, m, d ->
                calendar.set(y, m, d)
                Log.d(TAG, "Date has been set: $y-$m-$d")
                updateDate()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        findViewById<Button>(R.id.btn_set_rows).setOnClickListener {
            val countNew = edit_rows.text.toString().toInt()
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
            val name = edit_name.text.toString()
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
        btn_pick_date.text = text
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
        Log.d(TAG, "Updating adapter count")
        list_edit_matches.adapter.notifyDataSetChanged()
        edit_rows.setText(countMatches.toString())
    }

}

class EditMatchListRecyclerAdapter(val schedule: MatchSchedule) : RecyclerView.Adapter<EditMatchRowViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditMatchRowViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_edit_match_row, parent, false)
        return EditMatchRowViewHolder(view)
    }

    override fun getItemCount(): Int = schedule.count()

    override fun onBindViewHolder(holder: EditMatchRowViewHolder, position: Int) {
        holder.attachTo(schedule[position], position + 1)
    }

}

class EditMatchRowViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnFocusChangeListener {
    val textMatchNumber = v.findViewById<TextView>(R.id.label_match_number)
    val editRed1 = v.findViewById<EditText>(R.id.edit_team_red1)
    val editRed2 = v.findViewById<EditText>(R.id.edit_team_red2)
    val editRed3 = v.findViewById<EditText>(R.id.edit_team_red3)
    val editBlue1 = v.findViewById<EditText>(R.id.edit_team_blue1)
    val editBlue2 = v.findViewById<EditText>(R.id.edit_team_blue2)
    val editBlue3 = v.findViewById<EditText>(R.id.edit_team_blue3)

    private lateinit var match: Match

    init {
        listOf(editRed1, editRed2, editRed3, editBlue1, editBlue2, editBlue3).forEach {
            it.onFocusChangeListener = this
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus) return
        v as EditText
        when (v.id) {
            R.id.edit_team_red1 -> {
                match.red.alliance.a = v.text.toString().toInt()
            }
            R.id.edit_team_red2 -> {
                match.red.alliance.b = v.text.toString().toInt()
            }
            R.id.edit_team_red3 -> {
                match.red.alliance.c = v.text.toString().toInt()
            }
            R.id.edit_team_blue1 -> {
                match.blue.alliance.a = v.text.toString().toInt()
            }
            R.id.edit_team_blue2 -> {
                match.blue.alliance.b = v.text.toString().toInt()
            }
            R.id.edit_team_blue3 -> {
                match.blue.alliance.c = v.text.toString().toInt()
            }
        }
    }

    fun attachTo(match: Match, number: Int) {
        this.match = match

        textMatchNumber.text = number.toString() + "."
        editRed1.setText(match.red.alliance.a.toString())
        editRed2.setText(match.red.alliance.b.toString())
        editRed3.setText(match.red.alliance.c.toString())
        editBlue1.setText(match.blue.alliance.a.toString())
        editBlue2.setText(match.blue.alliance.b.toString())
        editBlue3.setText(match.blue.alliance.c.toString())
    }

}