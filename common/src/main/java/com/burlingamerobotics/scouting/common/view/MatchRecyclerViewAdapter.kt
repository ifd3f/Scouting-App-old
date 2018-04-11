package com.burlingamerobotics.scouting.common.view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.burlingamerobotics.scouting.common.R
import com.burlingamerobotics.scouting.shared.data.GameResult
import com.burlingamerobotics.scouting.shared.data.Match

class MatchRecyclerViewAdapter(val matches: List<Match>, val onClickCb: (Int) -> Unit = {})
    : RecyclerView.Adapter<MatchViewHolder>(), View.OnClickListener {

    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView = recyclerView!!
    }

    override fun onClick(v: View?) {
        onClickCb(recyclerView.getChildLayoutPosition(v))
    }

    override fun getItemCount(): Int {
        return matches.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.item_show_match_row, parent, false)
        return MatchViewHolder(this, view)
    }

    override fun onBindViewHolder(vh: MatchViewHolder, position: Int) {
        vh.bindMatch(matches[position])
    }

}

class MatchViewHolder(adapter: MatchRecyclerViewAdapter, view: View) : RecyclerView.ViewHolder(view) {
    val matchNumber = view.findViewById<TextView>(R.id.label_match_number)
    val redScore = view.findViewById<TextView>(R.id.text_alliance_red_score)
    val blueScore = view.findViewById<TextView>(R.id.text_alliance_blue_score)
    val textResult = view.findViewById<TextView>(R.id.text_match_result)
    val bgResult = view.findViewById<View>(R.id.div_match_result)

    init {
        view.setOnClickListener(adapter)
    }

    val redNumbers = listOf(
            view.findViewById<TextView>(R.id.text_team_red1),
            view.findViewById<TextView>(R.id.text_team_red2),
            view.findViewById<TextView>(R.id.text_team_red3)
    )
    val blueNumbers = listOf(
            view.findViewById<TextView>(R.id.text_team_blue1),
            view.findViewById<TextView>(R.id.text_team_blue2),
            view.findViewById<TextView>(R.id.text_team_blue3)
    )

    fun bindMatch(match: Match) {
        matchNumber.text = match.number.toString()
        redScore.text = match.red.points.toString()
        blueScore.text = match.blue.points.toString()
        redNumbers.zip(match.red.alliance).map { (text, num) ->
            text.text = num.toString()
        }
        blueNumbers.zip(match.blue.alliance).map { (text, num) ->
            text.text = num.toString()
        }
        textResult.setText(when (match.matchResult) {
            GameResult.RED_VICTORY -> R.string.matchres_red
            GameResult.BLUE_VICTORY -> R.string.matchres_blue
            GameResult.DRAW -> R.string.matchres_draw
            null -> R.string.matchres_scheduled
        })
        bgResult.setBackgroundResource(when (match.matchResult) {
            GameResult.RED_VICTORY -> R.color.team_red
            GameResult.BLUE_VICTORY -> R.color.team_blue
            else -> R.color.team_neutral
        })
    }
}
