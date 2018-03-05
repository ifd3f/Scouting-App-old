package com.burlingamerobotics.scouting.client.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.client.ScoutingClient
import com.burlingamerobotics.scouting.client.dialog.TeamEditDialog
import com.burlingamerobotics.scouting.common.Utils
import com.burlingamerobotics.scouting.common.data.Team
import com.burlingamerobotics.scouting.common.protocol.EventTeamChange
import com.burlingamerobotics.scouting.common.protocol.PostTeamInfo
import com.burlingamerobotics.scouting.common.protocol.TeamListRequest


class TeamListFragment : Fragment() {

    val TAG = "TeamListFragment"

    lateinit var lvTeamList: ListView
    lateinit var teamList: List<Team>
    lateinit var refresher: SwipeRefreshLayout

    var sorting = Team::number

    private val refreshHandler = Handler {
        val list = it.obj
        Log.d(TAG, "refreshHandler received $list")
        list as List<Team>
        teamList = list.sortedBy(sorting)
        Log.i(TAG, "Received request to refresh team list, with ${teamList.size} teams")
        lvTeamList.adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, teamList)
        refresher.isRefreshing = false
        true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.fragment_team_list, container, false)

        refresher = view.findViewById(R.id.refresh_list_teams)
        lvTeamList = view.findViewById(R.id.list_teams)

        view.findViewById<FloatingActionButton>(R.id.btn_add_team).setOnClickListener {
            Log.i(TAG, "User pressed add team button, creating edit dialog")
            TeamEditDialog(activity) {
                if (it != null) {
                    Log.i(TAG, "Received $it from dialog, posting")
                    ScoutingClient.blockingPost(PostTeamInfo(it))
                } else {
                    Log.i(TAG, "Dialog to edit was canceled")
                }
            }.show()
        }

        refresher.setOnRefreshListener {
            refresh()
        }

        ScoutingClient.eventListener.registerListener {
            when (it) {
                is EventTeamChange -> refresh(teamList + it.team)
            }
        }

        lvTeamList.setOnItemClickListener { parent, _, position, id ->
            Log.i(TAG, "selected $position which corresponds to ${teamList[position]}")
        }

        refresh()
        return view
    }

    fun refresh() {
        Log.d(TAG, "Requesting team data")
        Utils.ioExecutor.submit {
            refresh(ScoutingClient.blockingRequest(TeamListRequest))
        }
    }

    fun refresh(list: List<Team>) {
        Log.d(TAG, "Dispatching refresh message")
        refreshHandler.sendMessage(Message().apply {
            obj = list
        })
    }

}
