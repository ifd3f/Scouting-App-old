package com.burlingamerobotics.scouting.server.activity

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import com.burlingamerobotics.scouting.common.INTENT_CLIENT_CONNECTED
import com.burlingamerobotics.scouting.common.SCOUTING_UUID
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.server.R
import com.burlingamerobotics.scouting.server.io.ScoutingClient
import com.burlingamerobotics.scouting.server.io.ScoutingServerService

class ServerManagerActivity : AppCompatActivity() {

    lateinit var btAdapter: BluetoothAdapter
    lateinit var lvClients: ListView
    lateinit var switchStartServer: Switch
    lateinit var competition: Competition
    lateinit var txtCompetitionName: TextView

    val msgRefreshListHandler = Handler({ msg ->
        refreshList()
        true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_manager)
        //setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        competition = intent.getSerializableExtra("competition") as Competition

        lvClients = findViewById(R.id.list_connected_clients)
        txtCompetitionName = findViewById(R.id.text_competition_name)
        txtCompetitionName.text = competition.name

        switchStartServer = findViewById(R.id.switch_server)
        switchStartServer.setOnCheckedChangeListener { buttonView, isChecked ->
            setServerState(isChecked)
        }

        val itf = IntentFilter(INTENT_CLIENT_CONNECTED)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent!!.action == INTENT_CLIENT_CONNECTED) {
                    val client = intent.getSerializableExtra("client")
                    refreshList()
                }
            }
        }, itf)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setServerState(false)
    }

    private fun refreshList() {
        Log.d("MasterMgmt", "Refreshing connected clients list")
        val clients: List<ScoutingClient> = TODO("add way to get list of clients")
        clients.forEach {
            Log.d("MasterMgmt", "Found $it")
        }
        lvClients.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, clients.map { it.displayName })
    }

    private fun setServerState(state: Boolean) {
        if (state) {
            Log.i("MasterMgmt", "Starting scouting server")
            val serverSocket = btAdapter.listenUsingRfcommWithServiceRecord("Scouting Server", SCOUTING_UUID)
            startService(Intent(this, ScoutingServerService::class.java).apply {
                putExtra("competition", competition.uuid)
            })
        } else {
            Log.i("MasterMgmt", "Stopping scouting server")
            stopService(Intent(this, ScoutingServerService::class.java))
        }
    }

}
