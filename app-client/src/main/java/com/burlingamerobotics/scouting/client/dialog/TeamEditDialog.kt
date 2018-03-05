package com.burlingamerobotics.scouting.client.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.common.data.Team

class TeamEditDialog(context: Context, val team: Team? = null, val onFinished: (Team?) -> Unit) : Dialog(context) {

    val TAG = "TeamEditDialog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_team_edit)

        Log.d(TAG, "Creating dialog")

        val editName = findViewById<EditText>(R.id.edit_team_name)
        val editNumber = findViewById<EditText>(R.id.edit_team_number)

        editName.setText(team?.name ?: "")
        editNumber.setText(team?.number?.toString() ?: "")

        findViewById<Button>(R.id.btn_submit).setOnClickListener {
            onFinished(Team(editNumber.text.toString().toInt(), editName.text.toString()))
            dismiss()
        }

        findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            onFinished(null)
        }
    }

}