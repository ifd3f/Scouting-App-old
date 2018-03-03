package com.burlingamerobotics.scouting.client.fragment

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.common.data.Match


class MatchInfoAdapter(val match: Match) : RecyclerView.Adapter<MatchAttrViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MatchAttrViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_data_row, parent, false)
        return MatchAttrViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchAttrViewHolder?, position: Int) {
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

class MatchAttrViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val labelView: TextView = itemView.findViewById(R.id.text_attr_name)
    val dataView: TextView = itemView.findViewById(R.id.text_attr_data)
}