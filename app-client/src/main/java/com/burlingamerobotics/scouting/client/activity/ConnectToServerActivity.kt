package com.burlingamerobotics.scouting.client.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.burlingamerobotics.scouting.client.R
import com.burlingamerobotics.scouting.client.io.*
import com.burlingamerobotics.scouting.common.Utils
import java.io.IOException

class ConnectToServerActivity : AppCompatActivity() {

    lateinit var swipeRefresh: SwipeRefreshLayout
    lateinit var btAdapter: BluetoothAdapter
    lateinit var lvServers: ListView
    lateinit var lsServers: List<ServerData>
    lateinit var serviceWrapper: ClientServiceWrapper

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
            try {
                val intent = serv.getServiceIntent(this)!!
                Utils.ioExecutor.execute {
                    Log.i("ClientConnect", "Attempting to connect to $serv")
                    startService(intent)
                    serviceWrapper = ClientServiceWrapper(this)
                    try {
                        serviceWrapper.connect()
                        Log.i("ClientConnect", "Connection success! Starting BrowserActivity")
                        startActivity(Intent(this, BrowserActivity::class.java))
                    } catch (ex: IOException) {
                        Log.e("ClientConnect", "Failed to connect to $serv!", ex)
                        fireToast("Failed to connect!")
                        stopService(Intent(this, ClientService::class.java))
                    }
                }

            } catch (ex: Exception) {
                fireToast("Failed to connect!")
                when (ex) {
                    is IOException -> {
                        Log.e("ClientConnect", "Failed to connect to $serv: Bluetooth unavailable", ex)
                    }
                    else -> throw ex
                }
            }
        }

        swipeRefresh.setOnRefreshListener {
            Log.i("ClientConnect", "Refreshing BT devices")
            refreshServers()
        }

        refreshServers()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceWrapper.close()
    }

    fun refreshServers() {
        lsServers = listServers()
        lsServers.forEach {
            Log.d("ClientConnect", "Found $it")
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
