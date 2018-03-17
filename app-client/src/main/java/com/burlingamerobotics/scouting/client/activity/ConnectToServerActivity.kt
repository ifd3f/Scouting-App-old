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
import com.burlingamerobotics.scouting.client.io.ClientService
import com.burlingamerobotics.scouting.client.io.ClientServiceWrapper
import com.burlingamerobotics.scouting.common.INTENT_START_SCOUTING_CLIENT_BLUETOOTH
import com.burlingamerobotics.scouting.common.SCOUTING_UUID
import com.burlingamerobotics.scouting.common.Utils
import java.io.IOException

class ConnectToServerActivity : AppCompatActivity() {

    lateinit var btAdapter: BluetoothAdapter
    lateinit var btListView: ListView
    lateinit var btDevices: List<BluetoothDevice>
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
        btListView = findViewById(R.id.bt_list)

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.client_connect_swiperefresh)

        btListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val dev = btDevices[position]
            try {
                val sock = dev.createRfcommSocketToServiceRecord(SCOUTING_UUID)
                Utils.ioExecutor.execute {
                    Log.i("ClientConnect", "Attempting to connect to $dev")
                    try {
                        sock.connect()
                        startService(Intent(this, ClientService::class.java).apply {
                            action = INTENT_START_SCOUTING_CLIENT_BLUETOOTH
                            putExtra("device", dev)
                        })
                        serviceWrapper = ClientServiceWrapper(this)
                        Log.i("ClientConnect", "  Connection success!")
                        startActivity(Intent(this, BrowserActivity::class.java))
                    } catch (ex: IOException) {
                        Log.e("ClientConnect", "Failed to connect to $dev!", ex)
                        fireToast("Failed to connect!")
                    }
                }

            } catch (ex: Exception) {
                fireToast("Failed to connect!")
                when (ex) {
                    is IOException -> {
                        Log.e("ClientConnect", "Failed to connect to $dev: Bluetooth unavailable", ex)
                    }
                    else -> throw ex
                }
            }
        }

        swipeRefresh.setOnRefreshListener {
            Log.i("ClientConnect", "Refreshing BT devices")
            refreshBondedDevices()
            swipeRefresh.isRefreshing = false
        }

        refreshBondedDevices()

        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun refreshBondedDevices() {
        btDevices = btAdapter.bondedDevices.toList().filter { it.bondState == BluetoothDevice.BOND_BONDED }
        btDevices.forEach {
            Log.d("ClientConnect", "Found ${it.name} at ${it.address}")
        }
        btListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, btDevices.map {
            "${it.name} at ${it.address}"
        })
    }

    fun fireToast(text: String) {
        val msg = toastHandler.obtainMessage()
        val bundle = Bundle()
        bundle.putString("text", text)
        msg.data = bundle
        toastHandler.sendMessage(msg)
    }

}
