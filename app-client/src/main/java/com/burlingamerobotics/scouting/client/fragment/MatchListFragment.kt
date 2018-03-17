package com.burlingamerobotics.scouting.client.fragment

import android.content.Context
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
import com.burlingamerobotics.scouting.client.io.ClientServiceWrapper
import com.burlingamerobotics.scouting.common.Utils
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.common.protocol.CompetitionRequest
import com.burlingamerobotics.scouting.common.protocol.QualifierMatchRequest
import com.burlingamerobotics.scouting.common.view.MatchRecyclerViewAdapter

/**
 * Lists all the matches at a certain competition.
 */
class MatchListFragment : Fragment() {

    val TAG = "MatchListFragment"
    fun getViewAdapter(comp: Competition) = MatchRecyclerViewAdapter(comp.qualifiers) { i ->
        Utils.ioExecutor.execute {
            Log.i(TAG, "User selected match at position $i")
            val match = serviceWrapper.blockingRequest(QualifierMatchRequest(i))
            fragmentManager.beginTransaction()
                    .replace(R.id.client_main_fragment_container, MatchInfoFragment.newInstance(match), "match_info")
                    .addToBackStack(null)
                    .commit()
        }
    }

    lateinit var lvMatches: RecyclerView
    lateinit var refresher: SwipeRefreshLayout
    lateinit var serviceWrapper: ClientServiceWrapper
    //lateinit var matches: Array<Match>

    private val refreshHandler = Handler({ msg ->
        val comp = msg.obj as Competition
        Log.d(TAG, "Received refresh signal with $comp")
        lvMatches.adapter = getViewAdapter(comp)
        refresher.isRefreshing = false
        true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        serviceWrapper = ClientServiceWrapper(context)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_match_list, container, false)

        lvMatches = view.findViewById<RecyclerView>(R.id.list_matches)
        refresher = view.findViewById<SwipeRefreshLayout>(R.id.refresh_list_matches)

        lvMatches.layoutManager = LinearLayoutManager(context)
        refresher.setOnRefreshListener { refreshMatches() }

        refreshMatches()

        return view
    }

    override fun onResume() {
        super.onResume()
        refreshMatches()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceWrapper.close()
    }

    fun refreshMatches() {
        Log.d(TAG, "Refreshing")
        refresher.isRefreshing = true
        Utils.ioExecutor.execute {
            val obj = serviceWrapper.blockingRequest(CompetitionRequest)
            refreshHandler.sendMessage(Message().also {
                it.obj = obj
            })
        }
    }

}
