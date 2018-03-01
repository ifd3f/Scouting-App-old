package com.burlingamerobotics.scouting.fragment

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.burlingamerobotics.scouting.R

import com.burlingamerobotics.scouting.fragment.MatchListFragment.OnListFragmentInteractionListener
import com.burlingamerobotics.scouting.fragment.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MyMatchRecyclerViewAdapter(private val mValues:List<DummyItem>, private val mListener:OnListFragmentInteractionListener?):RecyclerView.Adapter<MyMatchRecyclerViewAdapter.ViewHolder>() {

    public override fun onCreateViewHolder(parent:ViewGroup, viewType:Int):ViewHolder {
        val view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_match, parent, false)
        return ViewHolder(view)
    }

    public override fun onBindViewHolder(holder:ViewHolder, position:Int) {
        val item = mValues.get(position)
        holder.mItem = item
        holder.mIdView.setText(mValues.get(position).id)
        holder.mContentView.setText(mValues.get(position).content)

        holder.mView.setOnClickListener(object:View.OnClickListener {
            public override fun onClick(v:View) {
                if (null != mListener)
                {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(item)
                }
            }
        })
    }

    public override fun getItemCount():Int {
        return mValues.size
    }

    inner class ViewHolder( val mView:View):RecyclerView.ViewHolder(mView) {
        val mIdView:TextView
        val mContentView:TextView
        var mItem:DummyItem? = null

        init{
            mIdView = mView.findViewById(R.id.id)
            mContentView = mView.findViewById(R.id.content)
        }

        public override fun toString():String {
            return super.toString() + " '" + mContentView.getText() + "'"
        }
    }
}
