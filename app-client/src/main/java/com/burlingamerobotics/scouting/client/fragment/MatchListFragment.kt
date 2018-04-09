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
import com.burlingamerobotics.scouting.common.data.Match
import com.burlingamerobotics.scouting.common.protocol.CompetitionRequest
import com.burlingamerobotics.scouting.common.protocol.MatchRequest
import com.burlingamerobotics.scouting.common.view.MatchRecyclerViewAdapter

/**
 * Lists all the matches at a certain competition.
 */
class MatchListFragment : Fragment(), Handler.Callback {
    private val TAG = "MatchListFragment"

    private val MSG_REFRESH = 1032
    private val MSG_START_MDF = 431

    private lateinit var lvMatches: RecyclerView
    private lateinit var refresher: SwipeRefreshLayout
    lateinit var service: ScoutingClientServiceBinder
    //lateinit var matches: Array<Match>

    /**
     * Receives messages from other threads telling the main thread to do stuff
     */
    private val extThreadHandler = Handler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Starting")
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_match_list, container, false)

        lvMatches = view.findViewById(R.id.list_matches)
        refresher = view.findViewById(R.id.refresh_list_matches)

        lvMatches.layoutManager = LinearLayoutManager(context)
        refresher.setOnRefreshListener { refresh() }

        return view
    }

    override fun handleMessage(msg: Message?): Boolean {
        when (msg!!.what) {
            MSG_REFRESH -> {
                val comp = msg.obj as Competition
                Log.d(TAG, "Received REFRESH signal with $comp")
                lvMatches.recycledViewPool.clear()
                lvMatches.adapter = getViewAdapter(comp)
                lvMatches.adapter.notifyDataSetChanged()
                refresher.isRefreshing = false
            }
            MSG_START_MDF -> {
                val match = msg.obj as Match
                Log.d(TAG, "Received START_MDF signal with $match")
                fragmentManager.beginTransaction()
                        .replace(R.id.client_main_fragment_container,
                                MatchDetailFragment.newInstance(match, service),
                                "match_info")
                        .addToBackStack(null)
                        .commit()
            }
        }
        return true
    }

    fun refresh() {
        Log.d(TAG, "Refreshing")
        refresher.isRefreshing = true
        Utils.ioExecutor.execute {
            val obj = service.blockingRequest(CompetitionRequest())
            Log.d(TAG, "Received after request: $obj")
            extThreadHandler.sendMessage(Message.obtain().also {
                it.what = MSG_REFRESH
                it.obj = obj
            })
        }
    }

    private fun getViewAdapter(comp: Competition) = MatchRecyclerViewAdapter(comp.qualifiers.matches) { i ->
        Utils.ioExecutor.execute {
            Log.i(TAG, "User selected match at position $i")
            val match = service.blockingRequest(MatchRequest(i))
            this@MatchListFragment.extThreadHandler.sendMessage(Message.obtain().also {
                it.what = MSG_START_MDF
                it.obj = match
            })
        }
    }

}
