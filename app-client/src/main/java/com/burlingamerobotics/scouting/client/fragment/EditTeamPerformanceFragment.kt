package com.burlingamerobotics.scouting.client.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.common.data.TeamPerformance


/**
 * A simple [Fragment] subclass.
 */
class EditTeamPerformanceFragment : Fragment() {

    lateinit var perf: TeamPerformance

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_edit_team_performance, container, false)
    }

    companion object {
        fun create(perf: TeamPerformance): EditTeamPerformanceFragment {
            val frag = EditTeamPerformanceFragment()
            frag.perf = perf
            return frag
        }
    }

}
