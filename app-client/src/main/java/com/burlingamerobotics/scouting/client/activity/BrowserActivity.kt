package com.burlingamerobotics.scouting.client.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.client.fragment.MatchListFragment
import com.burlingamerobotics.scouting.client.fragment.TeamListFragment
import com.burlingamerobotics.scouting.client.io.ScoutingClientService
import com.burlingamerobotics.scouting.client.io.ScoutingClientServiceBinder
import kotlinx.android.synthetic.main.activity_browser.*

class BrowserActivity : AppCompatActivity(), ServiceConnection {

    private val TAG = "BrowserActivity"
    private lateinit var service: ScoutingClientServiceBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        //setSupportActionBar(toolbar)

        //val fragmentFrame = findViewById<FrameLayout>(R.id.client_main_fragment_container)

        Log.d(TAG, "Binding to service")
        bindService(Intent(this, ScoutingClientService::class.java), this, 0)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.i(TAG, "Successfully bound to service")
        this.service = service as ScoutingClientServiceBinder

        Log.d(TAG, "Creating navigation")
        navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_matches -> {
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.client_main_fragment_container, MatchListFragment.create(service), "match_list")
                            .commit()
                    supportActionBar!!.setTitle(R.string.title_matches)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_teams -> {
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.client_main_fragment_container, TeamListFragment.create(service), "team_list")
                            .commit()
                    supportActionBar!!.setTitle(R.string.title_teams)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_messaging -> {
                    supportActionBar!!.setTitle(R.string.title_chat)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })

        navigation.selectedItemId = R.id.navigation_matches

    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.i(TAG, "Unbound from service")
    }

}
