package com.burlingamerobotics.scouting.client.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.client.io.ScoutingClientServiceBinder
import com.burlingamerobotics.scouting.common.Utils
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.protocol.CompetitionRequest
import com.burlingamerobotics.scouting.common.protocol.QualifierMatchRequest
import com.burlingamerobotics.scouting.common.view.MatchRecyclerViewAdapter

/**
 * Lists all the matches at a certain competition.
 */
class MatchListFragment : Fragment() {

    private val TAG = "MatchListFragment"

    private lateinit var lvMatches: RecyclerView
    private lateinit var refresher: SwipeRefreshLayout
    private lateinit var service: ScoutingClientServiceBinder
    //lateinit var matches: Array<Match>

    private val refreshHandler = Handler({ msg ->
        val comp = msg.obj as Competition
        Log.d(TAG, "Received refresh signal with $comp")
        lvMatches.adapter = getViewAdapter(comp)
        refresher.isRefreshing = false
        true
    })

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_match_list, container, false)

        lvMatches = view.findViewById<RecyclerView>(R.id.list_matches)
        refresher = view.findViewById<SwipeRefreshLayout>(R.id.refresh_list_matches)

        lvMatches.layoutManager = LinearLayoutManager(context)
        refresher.setOnRefreshListener { refreshMatches() }

        return view
    }

    fun refreshMatches() {
        Log.d(TAG, "Refreshing")
        refresher.isRefreshing = true
        Utils.ioExecutor.execute {
            val obj = service.blockingRequest(CompetitionRequest)
            Log.d(TAG, "Received $obj")
            refreshHandler.sendMessage(Message.obtain().also {
                it.obj = obj
            })
        }
    }

    fun getViewAdapter(comp: Competition) = MatchRecyclerViewAdapter(comp.qualifiers) { i ->
        Utils.ioExecutor.execute {
            Log.i(TAG, "User selected match at position $i")
            val match = service.blockingRequest(QualifierMatchRequest(i))
            fragmentManager.beginTransaction()
                    .replace(R.id.client_main_fragment_container, MatchInfoFragment.newInstance(match), "match_info")
                    .addToBackStack(null)
                    .commit()
        }
    }

    companion object {
        fun create(binder: ScoutingClientServiceBinder): MatchListFragment {
            return MatchListFragment().apply {
                service = binder
            }
        }
    }

}
