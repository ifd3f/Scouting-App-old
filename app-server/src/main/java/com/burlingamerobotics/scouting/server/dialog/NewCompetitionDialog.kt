package com.burlingamerobotics.scouting.server.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.burlingamerobotics.scouting.server.R

class NewCompetitionDialog(context: Context) : Dialog(context) {

    val TAG = "NewCompDialog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_new_competition)

        Log.d(TAG, "Creating dialog")

        val edit = findViewById<EditText>(R.id.edit_tba_event)

        findViewById<Button>(R.id.btn_empty_competition).setOnClickListener {

        }

        findViewById<Button>(R.id.btn_tba_submit).setOnClickListener {

        }

    }

}