package com.burlingamerobotics.scouting.server.activity

import android.Manifest
import android.app.AlertDialog
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.net.Uri
import android.os.*
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import com.burlingamerobotics.scouting.common.INTENT_BIND_SERVER_WRAPPER
import com.burlingamerobotics.scouting.common.INTENT_CLIENT_CONNECTED
import com.burlingamerobotics.scouting.common.INTENT_CLIENT_DISCONNECTED
import com.burlingamerobotics.scouting.common.SCOUTING_UUID
import com.burlingamerobotics.scouting.common.data.Competition
import com.burlingamerobotics.scouting.server.R
import com.burlingamerobotics.scouting.server.io.ClientInfo
import com.burlingamerobotics.scouting.server.io.ScoutingServerService
import com.burlingamerobotics.scouting.server.io.ScoutingServerServiceWrapper
import java.io.File

class ServerManagerActivity : AppCompatActivity(), ServiceConnection {

    val TAG = "ServerManagerActivity"
    val clients: MutableList<ClientInfo> = arrayListOf()

    lateinit var lvClients: ListView
    lateinit var switchStartServer: Switch
    lateinit var competition: Competition
    lateinit var txtCompetitionName: TextView

    var serviceWrapper: ScoutingServerServiceWrapper? = null

    val msgRefreshListHandler = Handler({ msg ->
        refreshList()
        true
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_manager)
        //setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)

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
            R.id.action_export_csv -> {
                Log.i(TAG, "User wants to export a CSV")
                val viewEditPath = EditText(this)
                viewEditPath.setText(File("/storage/self/primary", "matchdata.csv").absolutePath)
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                val builder = AlertDialog.Builder(this)
                        .setView(viewEditPath)
                        .setPositiveButton("Export", { d, _ ->
                            Log.i(TAG, "User saving the file")
                            val out = File(viewEditPath.text.toString())
                            Log.d(TAG, "Location read from EditText: $out")
                            out.writeText("1,2,23,4")
                            Toast.makeText(this, "Created file: $out", Toast.LENGTH_SHORT).show()
                            d.dismiss()
                        })
                        .setNegativeButton("Cancel", { d, _ ->
                            Log.i(TAG, "User canceled the operation")
                            d.cancel()
                        })
                val dialog = builder.create()
                //dialog.findViewById<FrameLayout>(android.R.id.custom)!!.addView(viewEditPath, ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
                dialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun refreshList() {
        Log.d("MasterMgmt", "Refreshing connected clients list")
        lvClients.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, clients.map { it.displayName })
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder) {
        Log.i(TAG, "Service bound successfully!")
        serviceWrapper = ScoutingServerServiceWrapper(service)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        clients.clear()
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
            Log.d(TAG, "Binding to server with $intent")
            bindService(intent, this, Service.BIND_IMPORTANT)
        } else {
            Log.i("MasterMgmt", "Stopping scouting server")
            stopService(Intent(this, ScoutingServerService::class.java))
            clients.clear()
        }
    }

}
