package com.burlingamerobotics.scouting.activity

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import com.burlingamerobotics.scouting.ClientResponseThread
import com.burlingamerobotics.scouting.R
import com.burlingamerobotics.scouting.ScoutingServer
import com.burlingamerobotics.scouting.common.Constants
import com.burlingamerobotics.scouting.common.Utils
import kotlinx.android.synthetic.main.activity_server_manager.*

class ServerManagerActivity : AppCompatActivity() {

    lateinit var btAdapter: BluetoothAdapter
    lateinit var lvClients: ListView
    lateinit var btnStartServer: Button
    lateinit var txtServerIndicator: TextView

    val msgRefreshListHandler = Handler({ msg ->
        refreshList()
        true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_manager)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        lvClients = findViewById(R.id.list_connected_clients)

        btnStartServer = findViewById<Button>(R.id.btn_start_server)
        txtServerIndicator = findViewById(R.id.txt_server_indicator)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setServerState(false)
    }

    private fun refreshList() {
        Log.d("MasterMgmt", "Refreshing connected clients list")
        val clients = ScoutingServer.clients
        clients.forEach {
            Log.d("MasterMgmt", "Found ${it.device.address}")
        }
        lvClients.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, clients.map { it.device.name })
    }

    private fun setServerState(state: Boolean) {
        if (state) {
            txtServerIndicator.text = getString(R.string.server_is_on)
            btnStartServer.text = getString(R.string.stop_server)

            Log.i("MasterMgmt", "Starting bluetooth server")

            val serverSocket = btAdapter.listenUsingRfcommWithServiceRecord("Scouting Server", Constants.SCOUTING_UUID)
            Utils.ioExecutor.submit {
                for (i in 1..6) {
                    val client = ClientResponseThread(serverSocket.accept())
                    Log.i("MasterMgmt", "Bluetooth device at ${client.device.address} connected")
                    ScoutingServer.clients.add(client)
                    client.start()
                    msgRefreshListHandler.sendMessage(Message())
                }
            }

            btnStartServer.setOnClickListener {
                setServerState(false)
            }
        } else {
            Log.i("MasterMgmt", "Stopping bluetooth server...")
            txtServerIndicator.text = getString(R.string.server_is_off)
            btnStartServer.text = getString(R.string.start_server)

            btnStartServer.setOnClickListener { view ->
                setServerState(true)
            }
        }
    }

}