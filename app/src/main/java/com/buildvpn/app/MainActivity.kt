package com.buildvpn.app

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private var isConnected = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val btnConnect = findViewById<Button>(R.id.btnConnect)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        
        btnConnect.setOnClickListener {
            if (!isConnected) {
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    startActivityForResult(intent, 1001)
                } else {
                    startVpn()
                }
            } else {
                stopVpn()
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            startVpn()
        }
    }
    
    private fun startVpn() {
        startForegroundService(Intent(this, com.buildvpn.app.VpnService::class.java).apply {
            action = "START"
        })
        isConnected = true
        findViewById<Button>(R.id.btnConnect).text = "Disconnect"
        findViewById<TextView>(R.id.tvStatus).text = "Status: Connected"
        Toast.makeText(this, "VPN Connected!", Toast.LENGTH_SHORT).show()
    }
    
    private fun stopVpn() {
        startService(Intent(this, com.buildvpn.app.VpnService::class.java).apply {
            action = "STOP"
        })
        isConnected = false
        findViewById<Button>(R.id.btnConnect).text = "Connect"
        findViewById<TextView>(R.id.tvStatus).text = "Status: Disconnected"
    }
}
