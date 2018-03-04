package com.burlingamerobotics.scouting.client.fragment

import android.app.Fragment
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.client.ScoutingClient
import com.burlingamerobotics.scouting.common.Utils
import com.burlingamerobotics.scouting.common.data.Team
import com.burlingamerobotics.scouting.common.data.TeamListRequest


class TeamListFragment : Fragment() {

    val TAG = "TeamListFragment"

    lateinit var lvTeamList: ListView
    lateinit var teamList: List<Team>
    lateinit var refresher: SwipeRefreshLayout

    val refreshHandler = Handler {
        teamList = it.obj as List<Team>
        lvTeamList.adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, teamList)
        refresher.isRefreshing = false
        true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.fragment_team_list, container, false)

        refresher = view.findViewById(R.id.refresh_list_teams)
        lvTeamList = view.findViewById(R.id.list_teams)

        refresher.setOnRefreshListener {
            refresh()
        }

        lvTeamList.setOnItemClickListener { parent, view, position, id ->
            Log.i(TAG, "selected $position which corresponds to ${teamList[position]}")
        }

        refresh()
        return view
    }

    fun refresh() {
        refresher.isRefreshing = true
        Utils.ioExecutor.submit {
            refreshHandler.dispatchMessage(Message().apply {
                obj = ScoutingClient.blockingRequest(TeamListRequest)!!
            })
        }
    }

}
