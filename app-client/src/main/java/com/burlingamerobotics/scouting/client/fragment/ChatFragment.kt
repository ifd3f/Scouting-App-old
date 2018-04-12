package com.burlingamerobotics.scouting.client.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.burlingamerobotics.scouting.client.R


/**
 * A simple [Fragment] subclass.
 */
class ChatFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_window, container, false)
    }

}// Required empty public constructor
