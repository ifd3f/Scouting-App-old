package com.burlingamerobotics.scouting.client.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.client.activity.EditTeamPerformanceActivity
import com.burlingamerobotics.scouting.client.io.ScoutingClientServiceBinder
import com.burlingamerobotics.scouting.common.REQUEST_CODE_EDIT
import com.burlingamerobotics.scouting.shared.Utils
import com.burlingamerobotics.scouting.shared.data.Match
import com.burlingamerobotics.scouting.shared.data.TeamPerformance
import com.burlingamerobotics.scouting.shared.protocol.EditTeamPerformanceAction
import com.burlingamerobotics.scouting.shared.protocol.EndEditTeamPerformanceAction
import com.burlingamerobotics.scouting.shared.protocol.MatchRequest
import kotlinx.android.synthetic.main.fragment_match_detail.*

class MatchDetailFragment : Fragment(), View.OnLongClickListener, SwipeRefreshLayout.OnRefreshListener {

    private val TAG = "MatchDetailFragment"

    private val MSG_REFRESH = 834
    private val MSG_EDIT_FAILURE = 31289
    private val MSG_EDIT_SUCCESS = 23479

    private val threadHandler = Handler {
        when (it.what) {
            MSG_REFRESH -> {
                Log.d(TAG, "Handler received message to refresh")
                updateViews()
            }
            MSG_EDIT_FAILURE -> {
                Log.d(TAG, "Handler received message to toast a lock error")
                Toast.makeText(activity, "Someone else is editing ${it.arg1}'s performance!", Toast.LENGTH_SHORT).show()
            }
            MSG_EDIT_SUCCESS -> {
                Log.d(TAG, "Handler received a message to start ETP")
                startActivityForResult(Intent(context, EditTeamPerformanceActivity::class.java).apply {
                    putExtra("existing", it.obj as TeamPerformance)
                }, REQUEST_CODE_EDIT)
            }
        }
        true
    }
    private lateinit var matchData: Match
    private var service: ScoutingClientServiceBinder? = null

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        matchData = args!!.get("match") as Match
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_match_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "Attaching listeners to views")
        listOf(
                R.id.text_team_red1,
                R.id.text_team_red2,
                R.id.text_team_red3,
                R.id.text_team_blue1,
                R.id.text_team_blue2,
                R.id.text_team_blue3).forEach {
            view.findViewById<View>(it).setOnLongClickListener(this)
        }

        refresh_match_info.setOnRefreshListener(this)
        updateViews()
    }

    override fun onLongClick(v: View): Boolean {
        val cli = service!!

        val teamPerf = when (v.id) {
            R.id.text_team_red1 -> {
                matchData.red.teams[0]
            }
            R.id.text_team_red2 -> {
                matchData.red.teams[1]
            }
            R.id.text_team_red3 -> {
                matchData.red.teams[2]
            }
            R.id.text_team_blue1 -> {
                matchData.blue.teams[0]
            }
            R.id.text_team_blue2 -> {
                matchData.blue.teams[1]
            }
            R.id.text_team_blue3 -> {
                matchData.blue.teams[2]
            }
            else -> {
                throw IllegalArgumentException("View does not have an allowed ID!")
            }
        }
        Utils.ioExecutor.submit {
            val result = cli.blockingAction(EditTeamPerformanceAction(teamPerf.match, teamPerf.teamNumber))
            if (result.status) {
                Log.d(TAG, "We may edit the TeamPerformance")
                threadHandler.sendMessage(Message.obtain().apply {
                    what = MSG_EDIT_SUCCESS
                    obj = result.payload as TeamPerformance
                })
            } else {
                Log.d(TAG, "Someone else is editing the TeamPerformance")
                threadHandler.sendMessage(Message.obtain().apply {
                    what = MSG_EDIT_FAILURE
                    arg1 = teamPerf.teamNumber
                })
            }
        }
        return true

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                Log.i(TAG, "Received result back from ETPActivity")
                data!!
                val perf = data.getSerializableExtra("result") as TeamPerformance
                Utils.ioExecutor.submit {
                    val actionres = service!!.blockingAction(EndEditTeamPerformanceAction(perf.match, perf.teamNumber, perf))
                    Log.d(TAG, actionres.toString())
                }
            }
            Activity.RESULT_CANCELED -> {
                Log.i(TAG, "Received canceled result from ETPActivity")
                Utils.ioExecutor.submit {
                    service!!.blockingAction(EndEditTeamPerformanceAction())
                }
            }
        }
    }

    override fun onRefresh() {
        Log.i(TAG, "User wants to refresh")
        Utils.ioExecutor.submit {
            matchData = service!!.blockingRequest(MatchRequest(matchData.number))
            threadHandler.sendEmptyMessage(MSG_REFRESH)
        }
    }

    private fun updateViews() {
        Log.d(TAG, "Updating with $matchData")

        val redAlliance = matchData.red.alliance
        val blueAlliance = matchData.blue.alliance

        text_team_red1.text = redAlliance.a.toString()
        text_team_red2.text = redAlliance.b.toString()
        text_team_red3.text = redAlliance.c.toString()
        text_team_blue1.text = blueAlliance.a.toString()
        text_team_blue2.text = blueAlliance.b.toString()
        text_team_blue3.text = blueAlliance.c.toString()

        text_match_number.text = "Match %s".format(matchData.number.toString())
        text_alliance_red_score.text = matchData.red.points.toString()
        text_alliance_blue_score.text = matchData.blue.points.toString()

    }

    companion object {

        fun newInstance(match: Match, service: ScoutingClientServiceBinder): MatchDetailFragment {
            val fragment = MatchDetailFragment().also {
                it.service = service
            }
            val args = Bundle()
            args.putSerializable("match", match)
            fragment.arguments = args
            return fragment
        }

    }
}
