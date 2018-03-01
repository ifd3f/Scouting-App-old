package com.burlingamerobotics.scouting.activity

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.burlingamerobotics.scouting.R
import com.burlingamerobotics.scouting.fragment.MatchListFragment
import kotlinx.android.synthetic.main.activity_client_browser.*

class ClientBrowser : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_browser)
        //setSupportActionBar(toolbar)

        //val fragmentFrame = findViewById<FrameLayout>(R.id.client_main_fragment_container)

        navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_matches -> {
                    supportFragmentManager.beginTransaction().add(R.id.client_main_fragment_container, MatchListFragment()).commit()
                    supportActionBar!!.setTitle(R.string.title_matches)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_teams -> {
                    supportActionBar!!.setTitle(R.string.title_teams)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_competitions -> {
                    supportActionBar!!.setTitle(R.string.title_competitions)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        })

        navigation.selectedItemId = R.id.navigation_matches

    }


}
