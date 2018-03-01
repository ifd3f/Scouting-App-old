package com.burlingamerobotics.scouting.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.burlingamerobotics.scouting.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.use_as_client).setOnClickListener { view ->
            startActivity(Intent(this, ClientConnectActivity::class.java))
        }

        findViewById<Button>(R.id.use_as_server).setOnClickListener { view ->
            startActivity(Intent(this, MasterManagementActivity::class.java))
        }
    }
}
