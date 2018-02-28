package com.burlingamerobotics.scouting.activity

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.burlingamerobotics.scouting.Constants
import com.burlingamerobotics.scouting.R
import com.burlingamerobotics.scouting.Utils
import kotlinx.android.synthetic.main.activity_client_connect.*
import java.io.IOException

class ClientConnectActivity : AppCompatActivity() {

    lateinit var btAdapter: BluetoothAdapter
    lateinit var btList: ListView
    lateinit var btDevices: List<BluetoothDevice>
    lateinit var loadingIndicator: ProgressBar

    @SuppressLint("HandlerLeak")
    private val stateMsgHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            val bundle = msg!!.data
            val failureToast = bundle.getString("failureToast")
            if (failureToast != null) {
                Toast.makeText(this@ClientConnectActivity, failureToast, Toast.LENGTH_SHORT).show()
                return
            }
            when (bundle.getInt("progressBar")) {
                1 -> {
                    loadingIndicator.visibility = View.VISIBLE
                    loadingIndicator.progress = 13
                }
                2 -> {
                    loadingIndicator.visibility = View.INVISIBLE
                    loadingIndicator.progress = 0
                }
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
        loadingIndicator = findViewById(R.id.client_connect_connecting_indicator)

        btList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val dev = btDevices[position]
            try {
                val sock = dev.createRfcommSocketToServiceRecord(Constants.SCOUTING_UUID)
                Utils.ioExecutor.execute {
                    setProgBarState(true)
                    Log.i("ClientConnect", "Attempting to connect to $dev")
                    try {
                        sock.connect()
                    } catch (e: IOException) {
                        Log.e("ClientConnect", "Failed to connect to $dev for whatever reason!")
                        produceError("Failed to connect!")
                    }
                    setProgBarState(false)
                }

            } catch (ex: Exception) {
                produceError("Failed to connect!")
                when (ex) {
                    is IOException -> {
                        Log.e("ClientConnect", "Failed to connect to $dev: BT unavailable", ex)
                    }
                    is ExceptionInInitializerError -> {
                        Log.e("ClientConnect", "Failed to connect to $dev: wrong service", ex)
                    }
                    else -> throw ex
                }
            }
        }

        swipeRefresh.setOnRefreshListener {
            regenerateBondedDevices()
            swipeRefresh.isRefreshing = false
        }

        regenerateBondedDevices()

        //supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    fun regenerateBondedDevices() {
        btDevices = btAdapter.bondedDevices.toList().filter { it.bondState == BluetoothDevice.BOND_BONDED }
        btList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, btDevices.map {
            "${it.name} at ${it.address}"
        })
    }

    fun produceError(text: String) {
        val msg = stateMsgHandler.obtainMessage()
        val bundle = Bundle()
        bundle.putString("failureToast", text)
        msg.data = bundle
        stateMsgHandler.sendMessage(msg)
    }

    fun setProgBarState(state: Boolean) {
        val msg = stateMsgHandler.obtainMessage()
        val bundle = Bundle()
        bundle.putInt("progressBar", if (state) 1 else 2)
        msg.data = bundle
        stateMsgHandler.sendMessage(msg)
    }

}
