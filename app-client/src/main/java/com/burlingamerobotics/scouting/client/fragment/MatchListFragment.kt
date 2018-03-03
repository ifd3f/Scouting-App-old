package com.burlingamerobotics.scouting.client.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.client.ScoutingClient
import com.burlingamerobotics.scouting.common.Utils
import com.burlingamerobotics.scouting.common.data.QualifierMatchRequest

/**
 * Lists all the matches at a certain competition.
 */
class MatchListFragment : Fragment() {

    lateinit var lvMatches: ListView
    lateinit var refresher: SwipeRefreshLayout
    //lateinit var matches: Array<Match>

    private val refreshHandler = Handler({ msg ->
        refresher.isRefreshing = true
        val matches = ScoutingClient.getQualifiers().mapIndexed { index, match ->
            index
        }
        lvMatches.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, matches)
        refresher.isRefreshing = false
        true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_match_list, container, false)

        lvMatches = view.findViewById<ListView>(R.id.list_matches)
        refresher = view.findViewById<SwipeRefreshLayout>(R.id.refresh_list_matches)

        lvMatches.setOnItemClickListener { parent, _, position, id ->
            Utils.ioExecutor.execute {
                val match = ScoutingClient.blockingRequest(QualifierMatchRequest(position))!!
                fragmentManager.beginTransaction()
                        .replace(R.id.client_main_fragment_container, MatchInfoFragment.newInstance(match), "match_info")
                        .addToBackStack(null)
                        .commit()
            }
        }
        refresher.setOnRefreshListener { refreshMatches() }

        refreshMatches()

        return view
    }

    override fun onResume() {
        super.onResume()
        refreshMatches()
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    fun refreshMatches() {
        ScoutingClient.invalidateCache()
        Utils.ioExecutor.execute {
            val msg = Message()
            refreshHandler.sendMessage(msg)
        }
    }

}
