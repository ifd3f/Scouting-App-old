package com.burlingamerobotics.scouting.client.activity

import android.app.AlertDialog
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import com.burlingamerobotics.scouting.client.INTENT_CLIENT_DISCONNECTED
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.client.fragment.MatchListFragment
import com.burlingamerobotics.scouting.client.fragment.TeamListFragment
import com.burlingamerobotics.scouting.client.io.ScoutingClientService
import com.burlingamerobotics.scouting.client.io.ScoutingClientServiceBinder
import com.burlingamerobotics.scouting.common.KBroadcastReceiver
import com.burlingamerobotics.scouting.common.intentFilter
import com.burlingamerobotics.scouting.shared.*
import kotlinx.android.synthetic.main.activity_browser.*

class BrowserActivity : AppCompatActivity(), ServiceConnection {

    private val TAG = "BrowserActivity"

    private lateinit var service: ScoutingClientServiceBinder
    private lateinit var matchListFragment: MatchListFragment
    private lateinit var teamListFragment: TeamListFragment
    private lateinit var disconnectReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_browser)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)

        matchListFragment = MatchListFragment()
        teamListFragment = TeamListFragment()

        Log.d(TAG, "Setting browser_navigation selected listener")
        navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_matches -> {
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.client_main_fragment_container, matchListFragment, "match_list")
                            .commit()
                    supportActionBar!!.setTitle(R.string.title_matches)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_teams -> {
                    supportFragmentManager.beginTransaction()
                            .replace(R.id.client_main_fragment_container, teamListFragment, "team_list")
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

        disconnectReceiver = KBroadcastReceiver { _, i ->
            Log.i(TAG, "Client service disconnected")
            val reasonCode = i.getIntExtra("reason", DISCONNECT_REASON_NONE)
            val reason = when (reasonCode) {
                DISCONNECT_REASON_KICK -> "Kicked by server"
                DISCONNECT_REASON_BAN -> "Banned by server"
                DISCONNECT_REASON_SHUTDOWN -> "Server shutdown"
                DISCONNECT_REASON_TIMEOUT -> "Timed out"
                else -> "No reason given"
            }
            AlertDialog.Builder(this)
                    .setTitle("Disconnected")
                    .setView(TextView(this).apply {
                        text = "Reason: %s".format(reason)
                    })
                    .setPositiveButton("Connect to another server") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .setCancelable(false)
                    .create().show()
        }

        registerReceiver(disconnectReceiver, intentFilter(INTENT_CLIENT_DISCONNECTED))
    }

    override fun onStart() {
        super.onStart()

        Log.i(TAG, "Binding to service")
        bindService(Intent(this, ScoutingClientService::class.java), this, Service.BIND_ABOVE_CLIENT)
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "Unbinding from service")
        unbindService(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(disconnectReceiver)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            Log.i(TAG, "Back pressed with 0 fragment on back stack. User wants to DC")
            AlertDialog.Builder(this)
                    .setTitle("Do you want to disconnect?")
                    .setPositiveButton("Disconnect") { dialog, _ ->
                        Log.i(TAG, "User wants to disconnect")
                        dialog.dismiss()
                        super.onBackPressed()
                        service.disconnect()
                    }
                    .setNegativeButton("Back") { dialog, _ ->
                        Log.i(TAG, "User doesn't want to disconnect")
                        dialog.cancel()
                    }
                    .create().show()
        } else {
            Log.i(TAG, "Back pressed with lots of fragments on stack")
            super.onBackPressed()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.i(TAG, "Successfully bound to service")
        this.service = service as ScoutingClientServiceBinder

        matchListFragment.service = service
        teamListFragment.service = service

        navigation.selectedItemId = R.id.navigation_matches

    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.i(TAG, "Unbound from service")
    }

}
