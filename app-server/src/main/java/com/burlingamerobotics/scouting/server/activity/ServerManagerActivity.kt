package com.burlingamerobotics.scouting.server.activity

import android.Manifest
import android.app.AlertDialog
import android.app.Service
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.burlingamerobotics.scouting.common.INTENT_BIND_SERVER_WRAPPER
import com.burlingamerobotics.scouting.common.INTENT_CLIENT_CONNECTED
import com.burlingamerobotics.scouting.common.INTENT_CLIENT_DISCONNECTED
import com.burlingamerobotics.scouting.server.R
import com.burlingamerobotics.scouting.server.io.ClientInfo
import com.burlingamerobotics.scouting.server.io.ScoutingServerService
import com.burlingamerobotics.scouting.server.io.ScoutingServerServiceWrapper
import com.burlingamerobotics.scouting.shared.csv.CSVSerializer
import com.burlingamerobotics.scouting.shared.csv.createSerializers
import com.burlingamerobotics.scouting.shared.data.Competition
import com.burlingamerobotics.scouting.shared.data.Match
import kotlinx.android.synthetic.main.content_server_manager.*
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class ServerManagerActivity : AppCompatActivity() {

    val TAG = "ServerManagerActivity"
    val clients: MutableList<ClientInfo> = arrayListOf()

    lateinit var lvClients: ListView
    lateinit var competition: Competition
    lateinit var txtCompetitionName: TextView

    private var serviceWrapper: ScoutingServerServiceWrapper? = null

    val msgRefreshListHandler = Handler { _ ->
        refreshList()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_manager)
        //setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)

        competition = intent.getSerializableExtra("competition") as Competition

        lvClients = findViewById(R.id.list_connected_clients)
        txtCompetitionName = findViewById(R.id.text_competition_name)
        txtCompetitionName.text = competition.name

        switch_server.setOnCheckedChangeListener { _, isChecked ->
            setServerState(isChecked)
        }

        val itf = IntentFilter(INTENT_CLIENT_CONNECTED)
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                when (intent.action) {
                    INTENT_CLIENT_CONNECTED -> {
                        val client = intent.getSerializableExtra("client") as ClientInfo
                        Log.i(TAG, "Client connected: $client")
                        clients.add(client)
                        msgRefreshListHandler.sendEmptyMessage(0)
                    }
                    INTENT_CLIENT_DISCONNECTED -> {
                        val client = intent.getSerializableExtra("client") as ClientInfo
                        Log.i(TAG, "Client disconnected: $client")
                        clients.remove(client)
                        msgRefreshListHandler.sendEmptyMessage(0)
                    }
                }
            }
        }, itf)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setServerState(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_server_actions, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_export_matches_csv -> {
                Log.i(TAG, "User wants to export a CSV of matches")

                val comp = serviceWrapper?.getCompetition() ?: competition

                val viewEditPath = EditText(this)
                viewEditPath.setText(File("/storage/self/primary", "matchdata.csv").absolutePath)  // default path
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)

                val builder = AlertDialog.Builder(this)
                        .setView(viewEditPath)
                        .setPositiveButton("Export", { d, _ ->
                            Log.i(TAG, "User saving the file")
                            val out = File(viewEditPath.text.toString())
                            Log.d(TAG, "Location read from EditText: $out")
                            out.parentFile.mkdirs()
                            out.writeText(Match.CSV_SER.makeCSV(comp.qualifiers.performances))
                            Toast.makeText(this, "Created file: $out", Toast.LENGTH_SHORT).show()
                            d.dismiss()
                        })
                        .setNegativeButton("Cancel", { d, _ ->
                            Log.i(TAG, "User canceled the operation")
                            d.cancel()
                        })

                val dialog = builder.create()
                dialog.show()
                true
            }
            R.id.action_export_pits_csv -> {
                //Log.w(TAG, "User wants to export ")
                false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refreshList() {
        Log.d("MasterMgmt", "Refreshing connected clients list")
        lvClients.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, clients.map { it.displayName })
    }

    private fun setServerState(state: Boolean) {
        if (state) {
            Log.i("MasterMgmt", "Starting scouting server")
            //val serverSocket = btAdapter.listenUsingRfcommWithServiceRecord("Scouting Server", SCOUTING_UUID)
            startService(Intent(this, ScoutingServerService::class.java).apply {
                putExtra("competition", competition.uuid)
            })
            Log.d(TAG, "Server started successfully")
            val intent = Intent(this, ScoutingServerService::class.java)
            intent.action = INTENT_BIND_SERVER_WRAPPER
            serviceWrapper = ScoutingServerServiceWrapper()
            Log.d(TAG, "Binding to server with $intent")
            bindService(intent, serviceWrapper, Service.BIND_ABOVE_CLIENT)
        } else {
            Log.i("MasterMgmt", "Stopping scouting server")
            stopService(Intent(this, ScoutingServerService::class.java))
            clients.clear()
        }
    }

}
