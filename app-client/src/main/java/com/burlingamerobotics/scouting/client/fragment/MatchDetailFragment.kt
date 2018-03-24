package com.burlingamerobotics.scouting.client.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.client.activity.EditTeamPerformanceActivity
import com.burlingamerobotics.scouting.common.REQUEST_CODE_EDIT
import com.burlingamerobotics.scouting.common.data.Match
import kotlinx.android.synthetic.main.fragment_match_detail.*

class MatchDetailFragment : Fragment(), View.OnLongClickListener {
    val TAG = "MatchDetailFragment"

    lateinit var matchData: Match

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        matchData = args!!.get("match") as Match
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_match_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listOf(
                R.id.text_team_red1,
                R.id.text_team_red2,
                R.id.text_team_red3,
                R.id.text_team_blue1,
                R.id.text_team_blue2,
                R.id.text_team_blue3).forEach {
            view.findViewById<View>(it).setOnLongClickListener(this)
        }

        view.findViewById<TextView>(R.id.text_match_number).text = matchData.number.toString()
        view.findViewById<TextView>(R.id.text_alliance_red_score).text = matchData.red.points.toString()
        view.findViewById<TextView>(R.id.text_alliance_blue_score).text = matchData.blue.points.toString()
    }

    override fun onLongClick(v: View): Boolean {
        val teamPerf = when (v.id) {
            R.id.text_team_blue1 -> {
                matchData.blue.teams[0]
            }
            R.id.text_team_blue2 -> {
                matchData.blue.teams[1]
            }
            R.id.text_team_blue3 -> {
                matchData.blue.teams[2]
            }
            R.id.text_team_red1 -> {
                matchData.red.teams[0]
            }
            R.id.text_team_red2 -> {
                matchData.red.teams[1]
            }
            R.id.text_team_red3 -> {
                matchData.red.teams[2]
            }
            else -> {
                throw IllegalArgumentException("View does not have an allowed ID!")
            }
        }
        startActivityForResult(Intent(context, EditTeamPerformanceActivity::class.java).apply {
            putExtra("existing", teamPerf.teamNumber)
            putExtra("existing", teamPerf)
        }, REQUEST_CODE_EDIT)
        return true
    }

    companion object {

        fun newInstance(match: Match): MatchDetailFragment {
            val fragment = MatchDetailFragment()
            val args = Bundle()
            args.putSerializable("match", match)
            fragment.arguments = args
            return fragment
        }

    }
}
