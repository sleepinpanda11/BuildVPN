package com.buildvpn.app

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var btnConnect: Button
    private lateinit var tvStatus: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        btnConnect = findViewById(R.id.btnConnect)
        tvStatus = findViewById(R.id.tvStatus)
        
        btnConnect.setOnClickListener {
            if (!BuildVpnService.isRunning) {
                val intent = VpnService.prepare(this)
                if (intent != null) {
                    startActivityForResult(intent, 100)
                } else {
                    handleVpnAction("START")
                }
            } else {
                handleVpnAction("STOP")
            }
        }
        updateUI()
    }

    private fun handleVpnAction(action: String) {
        val intent = Intent(this, BuildVpnService::class.java).apply { this.action = action }
        startForegroundService(intent)
        btnConnect.postDelayed({ updateUI() }, 600)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            handleVpnAction("START")
        }
    }

    private fun updateUI() {
        val active = BuildVpnService.isRunning
        btnConnect.text = if (active) "Disconnect" else "Connect"
        tvStatus.text = if (active) "Status: Connected" else "Status: Disconnected"
        tvStatus.setTextColor(if (active) 0xFF4CAF50.toInt() else 0xFF888888.toInt())
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}
