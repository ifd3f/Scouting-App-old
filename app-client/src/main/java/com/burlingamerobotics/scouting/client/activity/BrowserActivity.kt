package com.burlingamerobotics.scouting.client.activity

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.client.fragment.MatchListFragment
import com.burlingamerobotics.scouting.client.fragment.TeamListFragment
import kotlinx.android.synthetic.main.activity_browser.*

class BrowserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
        //setSupportActionBar(toolbar)

        //val fragmentFrame = findViewById<FrameLayout>(R.id.client_main_fragment_container)

        navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_matches -> {
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.client_main_fragment_container, MatchListFragment(), "match_list")
                            .commit()
                    supportActionBar!!.setTitle(R.string.title_matches)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_teams -> {
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.client_main_fragment_container, TeamListFragment(), "team_list")
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

}
