package com.burlingamerobotics.scouting.client.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.common.data.Match

class MatchInfoFragment : Fragment() {

    lateinit var data: Match

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        data = args!!.get("match") as Match
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_match_info, container, false)
        val recycler = view.findViewById<RecyclerView>(R.id.list_match_attr)
        recycler.adapter = MatchInfoAdapter(data)
        recycler.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {
        private val ARG_MATCH = "match"

        fun newInstance(match: Match): MatchInfoFragment {
            val fragment = MatchInfoFragment()
            val args = Bundle()
            args.putSerializable(ARG_MATCH, match)
            fragment.arguments = args
            return fragment
        }

        class MatchInfoAdapter(val match: Match) : RecyclerView.Adapter<SimpleRowViewHolder>() {

            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): SimpleRowViewHolder {
                val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_data_row, parent, false)
                return SimpleRowViewHolder(view)
            }

            override fun onBindViewHolder(holder: SimpleRowViewHolder?, position: Int) {
                val pair = when (position) {
                    1 -> "Match Number" to match.number.toString()
                    else -> "Error" to "Error"
                }
                holder!!.labelView.text = pair.first
                holder.dataView.text = pair.second
            }

            override fun getItemCount(): Int {
                return 10
            }

        }

        class SimpleRowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val labelView: TextView = itemView.findViewById(R.id.text_attr_name)
            val dataView: TextView = itemView.findViewById(R.id.text_attr_data)
        }
    }
}
