package com.burlingamerobotics.scouting.client.fragment

import android.os.Bundle
import android.os.Handler
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
import com.burlingamerobotics.scouting.client.dialog.TeamEditDialog
import com.burlingamerobotics.scouting.client.io.ScoutingClientServiceBinder
import com.burlingamerobotics.scouting.common.Utils
import com.burlingamerobotics.scouting.common.data.Team
import com.burlingamerobotics.scouting.common.protocol.PostTeamInfo
import com.burlingamerobotics.scouting.common.protocol.TeamListRequest


class TeamListFragment : Fragment() {
    private val TAG = "TeamListFragment"

    private lateinit var lvTeamList: ListView
    private lateinit var teamList: List<Team>
    private lateinit var refresher: SwipeRefreshLayout
    private lateinit var service: ScoutingClientServiceBinder

    var sorting = Team::number

    private val refreshHandler = Handler {
        Log.i(TAG, "Received request to refresh team list")
        lvTeamList.adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, teamList.sortedBy(sorting))
        refresher.isRefreshing = false
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    service.post(PostTeamInfo(it))
                } else {
                    Log.i(TAG, "Dialog to edit was canceled")
                }
            }.show()
        }

        refresher.setOnRefreshListener {
            Log.i(TAG, "User wants to refresh")
            refresh()
        }

        lvTeamList.setOnItemClickListener { parent, _, position, id ->
            Log.i(TAG, "User selected $position which corresponds to ${teamList[position]}")
        }

        return view
    }

    fun refresh() {
        refresher.isRefreshing = true
        Utils.ioExecutor.submit {
            Log.d(TAG, "Requesting team data")
            teamList = service.blockingRequest(TeamListRequest)
            refreshHandler.sendEmptyMessage(0)
        }
    }

    companion object {
        fun create(binder: ScoutingClientServiceBinder): TeamListFragment {
            return TeamListFragment().apply {
                service = binder
            }
        }
    }

}
