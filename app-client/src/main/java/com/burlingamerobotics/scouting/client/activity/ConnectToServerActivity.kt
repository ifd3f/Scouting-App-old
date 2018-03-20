package com.burlingamerobotics.scouting.client.activity

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.client.io.*
import java.io.IOException

class ConnectToServerActivity : AppCompatActivity(), ServiceConnection {
    private val TAG = "ConnectToServer"

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var lvServers: ListView
    private lateinit var lsServers: List<ServerData>
    private var service: ScoutingClientServiceBinder? = null

    private val toastHandler = Handler(Handler.Callback { msg ->
        val bundle = msg!!.data
        val toastText = bundle.getString("text")
        if (toastText != null) {
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show()
        }
        true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)
        //setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        lvServers = findViewById(R.id.bt_list)

        swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.client_connect_swiperefresh)

        lvServers.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val serv = lsServers[position]
            Log.i(TAG, "User selected $serv at $position")
            try {
                Log.i(TAG, "Attempting to connect to server")
                service!!.connectTo(serv)
                Log.i(TAG, "Connection success! Starting BrowserActivity")
                startActivity(Intent(this, BrowserActivity::class.java))
            } catch (ex: IOException) {
                Log.e(TAG, "Failed to connect to server!", ex)
                fireToast("Failed to connect!")
                stopService(Intent(this, ScoutingClientService::class.java))
            }
        }

        swipeRefresh.setOnRefreshListener {
            Log.i(TAG, "Refreshing BT devices")
            refreshServers()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.d(TAG, "Bound to ScoutingClientService!")
        this.service = service!! as ScoutingClientServiceBinder
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.d(TAG, "Unbound from ScoutingClientService!")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "Starting and binding to ScoutingClientService")
        startService(Intent(this, ScoutingClientService::class.java))
        bindService(Intent(this, ScoutingClientService::class.java), this, Service.BIND_AUTO_CREATE)
        refreshServers()
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "Activity stopping, unbinding from ScoutingClientService")
        unbindService(this)
    }

    fun refreshServers() {
        lsServers = listServers()
        lsServers.forEach {
            Log.d(TAG, "Found $it")
        }
        lvServers.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lsServers.map {
            it.displayName
        })
        swipeRefresh.isRefreshing = false
    }

    fun fireToast(text: String) {
        val msg = toastHandler.obtainMessage()
        val bundle = Bundle()
        bundle.putString("text", text)
        msg.data = bundle
        toastHandler.sendMessage(msg)
    }

    fun listServers(): List<ServerData> {
        val bluetooth = btAdapter.bondedDevices.toList()
                .filter { it.bondState == BluetoothDevice.BOND_BONDED }
                .map { BluetoothServerData(it) }
        return listOf(LocalServerData) + bluetooth
    }

}
