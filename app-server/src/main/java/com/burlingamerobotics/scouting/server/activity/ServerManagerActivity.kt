package com.burlingamerobotics.scouting.server.activity

import android.Manifest
import android.app.AlertDialog
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.burlingamerobotics.scouting.server.INTENT_BIND_SERVER_WRAPPER
import com.burlingamerobotics.scouting.server.INTENT_SERVER_CLIENT_CONNECTED
import com.burlingamerobotics.scouting.server.INTENT_SERVER_CLIENT_DISCONNECTED
import com.burlingamerobotics.scouting.server.R
import com.burlingamerobotics.scouting.server.io.ClientInfo
import com.burlingamerobotics.scouting.server.io.ScoutingServerService
import com.burlingamerobotics.scouting.server.io.ScoutingServerServiceWrapper
import com.burlingamerobotics.scouting.shared.DISCONNECT_REASON_KICK
import com.burlingamerobotics.scouting.shared.data.Competition
import com.burlingamerobotics.scouting.shared.data.Match
import kotlinx.android.synthetic.main.content_server_manager.*
import java.io.File

class ServerManagerActivity : AppCompatActivity() {

    val TAG = "ServerManagerActivity"
    val clients: MutableList<ClientInfo> = arrayListOf()

    lateinit var competition: Competition

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

        text_competition_name.text = competition.name

        switch_server.setOnCheckedChangeListener { _, isChecked ->
            setServerState(isChecked)
        }

        val itf = IntentFilter().apply {
            addAction(INTENT_SERVER_CLIENT_CONNECTED)
            addAction(INTENT_SERVER_CLIENT_DISCONNECTED)
        }
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                when (intent.action) {
                    INTENT_SERVER_CLIENT_CONNECTED -> {
                        val client = intent.getSerializableExtra("client") as ClientInfo
                        Log.i(TAG, "Client connected: $client")
                        clients.add(client)
                        msgRefreshListHandler.sendEmptyMessage(0)
                    }
                    INTENT_SERVER_CLIENT_DISCONNECTED -> {
                        val client = intent.getSerializableExtra("client") as ClientInfo
                        Log.i(TAG, "Client disconnected: $client")
                        clients.remove(client)
                        msgRefreshListHandler.sendEmptyMessage(0)
                    }
                }
            }
        }, itf)

        list_connected_clients.setOnItemClickListener { _, _, position, _ ->
            Log.i(TAG, "User wants to do something with client at position $position")
            val client = clients[position]
            AlertDialog.Builder(this)
                    .setTitle("Client Actions")
                    .setNeutralButton("Cancel") { dialog, _ ->
                        dialog.cancel()
                    }
                    .setNegativeButton("Kick") { dialog, _ ->
                        Log.i(TAG, "User wants to kick $client")
                        serviceWrapper!!.disconnect(client.sessId, DISCONNECT_REASON_KICK)
                        Toast.makeText(this, "Kicked ${client.displayName}", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .setPositiveButton("Ban") { dialog, _ ->
                        Log.i(TAG, "User wants to ban $client (NYI)")
                        dialog.dismiss()
                    }
                    .create().show()
        }

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
        Log.d(TAG, "Refreshing connected clients list")
        list_connected_clients.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, clients.map { it.displayName })
    }

    private fun setServerState(state: Boolean) {
        if (state) {
            Log.i(TAG, "Starting scouting server")
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
