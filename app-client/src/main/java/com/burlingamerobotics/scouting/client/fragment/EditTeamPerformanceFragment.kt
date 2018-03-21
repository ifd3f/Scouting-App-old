package com.burlingamerobotics.scouting.client.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.common.data.*
import kotlinx.android.synthetic.main.fragment_edit_team_performance.*


/**
 * A fragment for editing [TeamPerformance]
 */
class EditTeamPerformanceFragment : Fragment() {

    private lateinit var perf: TeamPerformance

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_edit_team_performance, container, false)

        val teamNumber: Int = arguments.getInt("team")
        perf = (arguments.getSerializable("existing") as TeamPerformance?) ?: TeamPerformance(teamNumber)

        spinner_auto_start_pos.adapter = ArrayAdapter(
                context, android.R.layout.simple_spinner_dropdown_item,
                enumValues<StartPosition>().map { context.resources.getString(it.resId) })
        spinner_auto_cube_pos.adapter = ArrayAdapter(
                context, android.R.layout.simple_spinner_dropdown_item,
                enumValues<CubePosition>().map { context.resources.getString(it.resId) })

        val ratingAdapter = ArrayAdapter(
                context, android.R.layout.simple_spinner_dropdown_item,
                enumValues<Rating>().map { context.resources.getString(it.resId) }
        )
        spinner_rating_exchange.adapter = ratingAdapter
        spinner_rating_intake.adapter = ratingAdapter
        spinner_rating_scale.adapter = ratingAdapter
        spinner_rating_switch.adapter = ratingAdapter
        spinner_rating_defense.adapter = ratingAdapter

        spinner_auto_start_pos.setSelection(perf.autoStartPos.ordinal)
        spinner_auto_cube_pos.setSelection(perf.autoCubePlacement.ordinal)

        spinner_rating_exchange.setSelection(perf.ratingExchange.ordinal)
        spinner_rating_intake.setSelection(perf.ratingIntake.ordinal)
        spinner_rating_scale.setSelection(perf.ratingScale.ordinal)
        spinner_rating_switch.setSelection(perf.ratingSwitch.ordinal)
        spinner_rating_defense.setSelection(perf.ratingDefense.ordinal)

        edit_team_number.setText(teamNumber.toString())
        edit_auto_remaining_time.setText(perf.autoTimeRemaining.toString())
        edit_tele_cubes_exchange.setText(perf.teleCubesExchange.toString())

        perf.teleCubesOwnSwitch.writeTo(edit_tele_cubes_switch_hit, edit_tele_cubes_switch_miss)
        perf.teleCubesScale.writeTo(edit_tele_cubes_scale_hit, edit_tele_cubes_scale_miss)
        perf.teleCubesOppSwitch.writeTo(edit_tele_cubes_opp_hit, edit_tele_cubes_opp_miss)

        return view
    }

    fun validate() {
        edit_auto_remaining_time.text.toString()
    }

    companion object {
        fun CubeStats.writeTo(hit: EditText, miss: EditText) {
            hit.setText(this.hit.toString())
            miss.setText(this.miss.toString())
        }

        fun create(perf: TeamPerformance): EditTeamPerformanceFragment {
            val frag = EditTeamPerformanceFragment()
            frag.arguments = Bundle().apply {
                putInt("team", perf.teamNumber)
                putSerializable("existing", perf)
            }
            return frag
        }

        fun create(teamNumber: Int): EditTeamPerformanceFragment {
            val frag = EditTeamPerformanceFragment()
            frag.arguments = Bundle().apply {
                putInt("team", teamNumber)
            }
            return frag
        }
    }

}
