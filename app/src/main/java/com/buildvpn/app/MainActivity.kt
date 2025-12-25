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
            if (!VpnService.isRunning) {
                startVpn()
            } else {
                stopVpn()
            }
        }
        
        btnAddServer.setOnClickListener { showAddServerDialog() }
        
        spinnerServers.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (servers.isNotEmpty()) {
                    ConfigManager.setActiveServer(this@MainActivity, servers[pos].id)
                    updateUI()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        loadServers()
    }
    
    override fun onResume() {
        super.onResume()
        updateUI()
    }
    
    private fun loadServers() {
        servers = ConfigManager.getServers(this)
        
        if (servers.isEmpty()) {
            val demo = ServerConfig(
                id = UUID.randomUUID().toString(),
                name = "Demo Server",
                type = "WireGuard",
                address = "demo.example.com",
                port = 51820,
                config = "{}"
            )
            ConfigManager.saveServer(this, demo)
            ConfigManager.setActiveServer(this, demo.id)
            servers = ConfigManager.getServers(this)
        }
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, 
            servers.map { "${it.name} (${it.type})" })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerServers.adapter = adapter
        
        val active = ConfigManager.getActiveServer(this)
        if (active != null) {
            val idx = servers.indexOfFirst { it.id == active.id }
            if (idx >= 0) spinnerServers.setSelection(idx)
        }
        
        updateUI()
    }
    
    private fun showAddServerDialog() {
        val options = arrayOf("WireGuard", "VLESS", "Hysteria2", "Shadowsocks")
        
        AlertDialog.Builder(this)
            .setTitle("Add Server")
            .setItems(options) { _, which ->
                showServerInputDialog(options[which])
            }
            .show()
    }
    
    private fun showServerInputDialog(type: String) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }
        
        val etName = EditText(this).apply { hint = "Server Name" }
        val etAddress = EditText(this).apply { hint = "Server Address" }
        val etPort = EditText(this).apply { hint = "Port" }
        
        layout.addView(etName)
        layout.addView(etAddress)
        layout.addView(etPort)
        
        AlertDialog.Builder(this)
            .setTitle("Add $type Server")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val server = ServerConfig(
                    id = UUID.randomUUID().toString(),
                    name = etName.text.toString().ifEmpty { "$type Server" },
                    type = type,
                    address = etAddress.text.toString(),
                    port = etPort.text.toString().toIntOrNull() ?: 443,
                    config = "{}"
                )
                ConfigManager.saveServer(this, server)
                ConfigManager.setActiveServer(this, server.id)
                loadServers()
                Toast.makeText(this, "Server added!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun startVpn() {
        if (ConfigManager.getActiveServer(this) == null) {
            Toast.makeText(this, "Select a server first", Toast.LENGTH_SHORT).show()
            return
        }
        
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, 1001)
        } else {
            launchVpn()
        }
    }
    
    private fun stopVpn() {
        startService(Intent(this, VpnService::class.java).apply { action = "STOP" })
        btnConnect.postDelayed({ updateUI() }, 300)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            launchVpn()
        }
    }
    
    private fun launchVpn() {
        startForegroundService(Intent(this, VpnService::class.java).apply { action = "START" })
        btnConnect.postDelayed({ updateUI() }, 500)
        Toast.makeText(this, "VPN Connected!", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateUI() {
        val connected = VpnService.isRunning
        
        btnConnect.text = if (connected) "Disconnect" else "Connect"
        tvStatus.text = if (connected) "Status: Connected" else "Status: Disconnected"
        tvStatus.setTextColor(if (connected) 0xFF4CAF50.toInt() else 0xFF888888.toInt())
        
        val active = ConfigManager.getActiveServer(this)
        tvServer.text = active?.let { "${it.name} - ${it.address}" } ?: "No server"
    }
}
