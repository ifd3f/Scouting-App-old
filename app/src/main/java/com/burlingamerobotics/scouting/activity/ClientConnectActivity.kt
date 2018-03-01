package com.burlingamerobotics.scouting.activity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.burlingamerobotics.scouting.Constants
import com.burlingamerobotics.scouting.R
import com.burlingamerobotics.scouting.Utils
import com.burlingamerobotics.scouting.client.ScoutingClient
import kotlinx.android.synthetic.main.activity_client_connect.*
import java.io.IOException

class ClientConnectActivity : AppCompatActivity() {

    lateinit var btAdapter: BluetoothAdapter
    lateinit var btList: ListView
    lateinit var btDevices: List<BluetoothDevice>

    @SuppressLint("HandlerLeak")
    private val toastHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            val bundle = msg!!.data
            val toastText = bundle.getString("text")
            if (toastText != null) {
                Toast.makeText(this@ClientConnectActivity, toastText, Toast.LENGTH_SHORT).show()
                return
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_connect)
        setSupportActionBar(toolbar)

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        btList = findViewById(R.id.bt_list)

        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.client_connect_swiperefresh)

        btList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val dev = btDevices[position]
            try {
                val sock = dev.createRfcommSocketToServiceRecord(Constants.SCOUTING_UUID)
                Utils.ioExecutor.execute {
                    Log.i("ClientConnect", "Attempting to connect to $dev")
                    try {
                        sock.connect()
                        ScoutingClient.start(sock)
                        Log.i("ClientConnect", "  Connection success!")
                        startActivity(Intent(this, ClientBrowserActivity::class.java))
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
        btList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, btDevices.map {
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
