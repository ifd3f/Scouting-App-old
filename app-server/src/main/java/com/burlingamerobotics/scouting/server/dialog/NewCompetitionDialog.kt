package com.burlingamerobotics.scouting.server.dialog

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.burlingamerobotics.scouting.common.BlueAllianceAPI
import com.burlingamerobotics.scouting.common.REQUEST_CODE_EDIT
import com.burlingamerobotics.scouting.common.REQUEST_CODE_NEW
import com.burlingamerobotics.scouting.common.Utils
import com.burlingamerobotics.scouting.common.data.CompetitionBuilder
import com.burlingamerobotics.scouting.server.R
import com.burlingamerobotics.scouting.server.activity.CompetitionEditorActivity
import com.burlingamerobotics.scouting.server.activity.CompetitionSelectionActivity
import java.util.concurrent.Future

class NewCompetitionDialog(private val parent: CompetitionSelectionActivity) : Dialog(parent) {

    val TAG = "NewCompDialog"

    private val startEditorHandler: Handler = Handler { msg ->
        val (comp, isValid) = msg.obj as Pair<CompetitionBuilder, Boolean>
        if (isValid) {
            Log.i(TAG, "Starting editor with request to edit")
            parent.startActivityForResult(
                    Intent(parent, CompetitionEditorActivity::class.java).apply {
                        putExtra("competition", comp)
                        putExtra("request", REQUEST_CODE_EDIT)
                    },
                    REQUEST_CODE_EDIT
            )
            dismiss()
        } else {
            Log.e(TAG, "Error: it was an invalid event key!")
            btnEmpty.isClickable = true
            btnSubmit.isClickable = true
            spinner.visibility = View.INVISIBLE
        }
        true
    }

    lateinit var btnEmpty: Button
    lateinit var btnSubmit: Button
    lateinit var spinner: ProgressBar
    var fetchTask: Future<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_new_competition)

        Log.d(TAG, "Creating dialog")

        setTitle("New Competition")

        val editEvent = findViewById<EditText>(R.id.edit_tba_event)
        spinner = findViewById<ProgressBar>(R.id.spinner_loading_tba)

        btnEmpty = findViewById<Button>(R.id.btn_empty_competition)
        btnEmpty.setOnClickListener {
            parent.startActivityForResult(
                    Intent(parent, CompetitionEditorActivity::class.java).apply {
                        putExtra("request", REQUEST_CODE_NEW)
                    },
                    REQUEST_CODE_NEW
            )
            dismiss()
        }

        btnSubmit = findViewById<Button>(R.id.btn_tba_submit)
        btnSubmit.setOnClickListener {
            val event = editEvent.text.toString()
            Log.i(TAG, "User wants to fetch $event from TBA")
            btnEmpty.isClickable = false
            btnSubmit.isClickable = false
            editEvent.isActivated = false
            spinner.visibility = View.VISIBLE
            spinner.progress = 10
            fetchTask = Utils.ioExecutor.submit {
                val msg = Message()
                val result = BlueAllianceAPI.fetchCompetition(event)
                Log.d(TAG, "Got response: $result")
                msg.obj = Pair(result, result != null)
                startEditorHandler.sendMessage(msg)
            }
        }

    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "Closing NewCompetitionDialog")
        fetchTask?.cancel(true)
    }

}