package com.burlingamerobotics.scouting.fragment

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
import com.burlingamerobotics.scouting.R
import com.burlingamerobotics.scouting.Utils
import com.burlingamerobotics.scouting.client.ScoutingClient
import com.burlingamerobotics.scouting.data.MatchListRequest
import com.burlingamerobotics.scouting.data.SimpMatch

/**
 * Lists all the matches at a certain competition.
 */
class MatchListFragment : Fragment() {

    lateinit var matchesList: ListView
    lateinit var refresher: SwipeRefreshLayout

    private val refreshHandler = Handler({ msg ->
        refresher.isRefreshing = true
        @Suppress("UNCHECKED_CAST")
        val matches = msg.obj as List<SimpMatch>
        matchesList.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, matches.map {
            it.number
        })
        refresher.isRefreshing = false
        true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_match_list, container, false)

        matchesList = view.findViewById<ListView>(R.id.list_matches)
        refresher = view.findViewById<SwipeRefreshLayout>(R.id.refresh_list_matches)

        refresher.setOnRefreshListener {
            refreshMatches()
        }
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
        Utils.ioExecutor.execute {
            val msg = Message()
            msg.obj = ScoutingClient.blockingRequest(MatchListRequest(320))!!
            refreshHandler.sendMessage(msg)
        }
    }

}
