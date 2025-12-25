package com.buildvpn.app

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class MainActivity : AppCompatActivity() {
    
    private lateinit var btnConnect: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvServer: TextView
    private lateinit var spinnerServers: Spinner
    private lateinit var btnAddServer: Button
    private var servers = listOf<ServerConfig>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        btnConnect = findViewById(R.id.btnConnect)
        tvStatus = findViewById(R.id.tvStatus)
        tvServer = findViewById(R.id.tvServer)
        spinnerServers = findViewById(R.id.spinnerServers)
        btnAddServer = findViewById(R.id.btnAddServer)
        
        btnConnect.setOnClickListener {
            if (!MyVpnService.isRunning) {
                startVpnRequest()
            } else {
                stopVpn()
            }
        }
        
        loadServers()
    }

    private fun loadServers() {
        servers = ConfigManager.getServers(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, 
            servers.map { "${it.name} (${it.type})" })
        spinnerServers.adapter = adapter
        updateUI()
    }

    private fun startVpnRequest() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, 1001)
        } else {
            onActivityResult(1001, Activity.RESULT_OK, null)
        }
    }

    private fun stopVpn() {
        startService(Intent(this, MyVpnService::class.java).apply { action = "STOP" })
        btnConnect.postDelayed({ updateUI() }, 500)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            startForegroundService(Intent(this, MyVpnService::class.java).apply { action = "START" })
            btnConnect.postDelayed({ updateUI() }, 1000)
        }
    }

    private fun updateUI() {
        val connected = MyVpnService.isRunning
        btnConnect.text = if (connected) "Disconnect" else "Connect"
        tvStatus.text = if (connected) "Status: Connected" else "Status: Disconnected"
        tvStatus.setTextColor(if (connected) 0xFF4CAF50.toInt() else 0xFF888888.toInt())
    }
}
