package com.burlingamerobotics.scouting.client.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.common.data.Match

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MatchInfoFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MatchInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MatchInfoFragment : Fragment() {

    lateinit var data: Match

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        data = args!!.get("match") as Match
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_match_info, container, false)
        val recycler = view.findViewById<RecyclerView>(R.id.list_match_attr)
        recycler.adapter = MatchInfoAdapter(data)
        recycler.layoutManager = LinearLayoutManager(context)

        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {
        private val ARG_MATCH = "match"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MatchInfoFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(match: Match): MatchInfoFragment {
            val fragment = MatchInfoFragment()
            val args = Bundle()
            args.putSerializable(ARG_MATCH, match)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
