package com.burlingamerobotics.scouting.activity

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView
import com.burlingamerobotics.scouting.Constants
import com.burlingamerobotics.scouting.R
import com.burlingamerobotics.scouting.Utils
import com.burlingamerobotics.scouting.server.ClientResponseThread
import kotlinx.android.synthetic.main.activity_master_management.*

class MasterManagementActivity : AppCompatActivity() {

    lateinit var btAdapter: BluetoothAdapter
    lateinit var btList: ListView
    val btDevices: MutableList<ClientResponseThread> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_master_management)
        setSupportActionBar(toolbar)

        btAdapter = BluetoothAdapter.getDefaultAdapter()
        val serverSocket = btAdapter.listenUsingRfcommWithServiceRecord("Scouting Server", Constants.SCOUTING_UUID)
        for (i in 1..6) {
            Utils.ioExecutor.submit {
                btDevices.add(ClientResponseThread(serverSocket.accept()))
                refreshList()
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun refreshList() {
        btList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, btDevices.map {
            it.device.name
        })
    }

}
