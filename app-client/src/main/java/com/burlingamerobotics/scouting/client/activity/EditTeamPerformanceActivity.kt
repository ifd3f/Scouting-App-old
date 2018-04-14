package com.burlingamerobotics.scouting.client.activity


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.EditText
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.common.resId
import com.burlingamerobotics.scouting.shared.data.*
import kotlinx.android.synthetic.main.activity_edit_team_performance.*


/**
 * An activity for editing [TeamPerformance]
 */
class EditTeamPerformanceActivity : AppCompatActivity() {

    private val TAG = "EditTeamPerformance"

    private lateinit var perf: TeamPerformance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_ACTION_BAR)
        setContentView(R.layout.activity_edit_team_performance)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_dialog_close_dark)
        }

        Log.d(TAG, "Creating fields")

        perf = intent.extras.getSerializable("existing") as TeamPerformance

        toolbar.title = "Match ${perf.match}, Team ${perf.teamNumber}"

        spinner_auto_start_pos.adapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_dropdown_item,
                enumValues<StartPosition>().map { resources.getString(it.resId) })
        spinner_auto_cube_pos.adapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_dropdown_item,
                enumValues<CubePosition>().map { resources.getString(it.resId) })

        val ratingAdapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_dropdown_item,
                enumValues<Rating>().map { resources.getString(it.resId) }
        )
        spinner_rating_exchange.adapter = ratingAdapter
        spinner_rating_intake.adapter = ratingAdapter
        spinner_rating_scale.adapter = ratingAdapter
        spinner_rating_switch.adapter = ratingAdapter
        spinner_rating_defense.adapter = ratingAdapter

        Log.d(TAG, "Writing values to form fields")

        spinner_auto_start_pos.setSelection(perf.autoStartPos.ordinal)
        spinner_auto_cube_pos.setSelection(perf.autoCubePlacement.ordinal)

        spinner_rating_exchange.setSelection(perf.ratingExchange.ordinal)
        spinner_rating_intake.setSelection(perf.ratingIntake.ordinal)
        spinner_rating_scale.setSelection(perf.ratingScale.ordinal)
        spinner_rating_switch.setSelection(perf.ratingSwitch.ordinal)
        spinner_rating_defense.setSelection(perf.ratingDefense.ordinal)

        chk_baseline.isChecked = perf.autoCrossedLine
        edit_auto_remaining_time.setText(perf.autoTimeRemaining.toString())
        edit_tele_cubes_exchange.setText(perf.teleCubesExchange.toString())

        perf.teleCubesOwnSwitch.writeTo(edit_tele_cubes_switch_hit, edit_tele_cubes_switch_miss)
        perf.teleCubesScale.writeTo(edit_tele_cubes_scale_hit, edit_tele_cubes_scale_miss)
        perf.teleCubesOppSwitch.writeTo(edit_tele_cubes_opp_hit, edit_tele_cubes_opp_miss)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_team_performance, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_save -> {
                Log.d(TAG, "User wants to save")
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra("result", build())
                })
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun build(): TeamPerformance {
        val ratings = enumValues<Rating>()

        return TeamPerformance(
                match = perf.match,
                team = perf.team,
                teamNumber = perf.teamNumber,
                autoStartPos = enumValues<StartPosition>()[spinner_auto_start_pos.selectedItemPosition],
                autoTimeRemaining = edit_auto_remaining_time.text.toString().toInt(),
                autoCrossedLine = chk_baseline.isChecked,
                autoCubePlacement = enumValues<CubePosition>()[spinner_auto_cube_pos.selectedItemPosition],
                ratingDefense = ratings[spinner_rating_defense.selectedItemPosition],
                ratingSwitch = ratings[spinner_rating_switch.selectedItemPosition],
                ratingScale = ratings[spinner_rating_scale.selectedItemPosition],
                ratingIntake = ratings[spinner_rating_intake.selectedItemPosition],
                ratingExchange = ratings[spinner_rating_exchange.selectedItemPosition]
        ).apply {
            teleCubesOwnSwitch.readFrom(edit_tele_cubes_switch_hit, edit_tele_cubes_switch_miss)
            teleCubesScale.readFrom(edit_tele_cubes_scale_hit, edit_tele_cubes_scale_miss)
            teleCubesOppSwitch.readFrom(edit_tele_cubes_opp_hit, edit_tele_cubes_opp_miss)
        }
    }

    companion object {
        fun CubeStats.writeTo(hit: EditText, miss: EditText) {
            hit.setText(this.hit.toString())
            miss.setText(this.miss.toString())
        }

        fun CubeStats.readFrom(hit: EditText, miss: EditText) {
            this.hit = hit.text.toString().toInt()
            this.miss = miss.text.toString().toInt()
        }
    }

}
