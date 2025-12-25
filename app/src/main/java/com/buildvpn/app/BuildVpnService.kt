package com.buildvpn.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat

class BuildVpnService : VpnService() {
    
    companion object {
        private const val CHANNEL_ID = "vpn_status_channel"
        private const val NOTIFICATION_ID = 101
        
        @Volatile
        var isRunning = false // Thread-safe state for UI
    }
    
    private var vpnInterface: ParcelFileDescriptor? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> startVpn()
            "STOP" -> stopVpn()
        }
        return START_STICKY
    }
    
    private fun startVpn() {
        val server = ConfigManager.getActiveServer(this) ?: return stopSelf()
        
        val builder = Builder()
            .setSession("BuildVPN: ${server.name}")
            .addAddress("10.0.0.2", 32)
            .addRoute("0.0.0.0", 0)
            .addDnsServer("8.8.8.8")
            .setMtu(1500)
        
        try {
            builder.addDisallowedApplication(packageName)
            vpnInterface = builder.establish()
            
            if (vpnInterface != null) {
                isRunning = true
                startForeground(NOTIFICATION_ID, createNotification("Connected to ${server.name}"))
            } else {
                stopSelf()
            }
        } catch (e: Exception) {
            isRunning = false
            stopSelf()
        }
    }
    
    private fun stopVpn() {
        isRunning = false
        try {
            vpnInterface?.close()
        } catch (e: Exception) {}
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(content: String): Notification {
        val stopIntent = Intent(this, BuildVpnService::class.java).apply { action = "STOP" }
        val pending = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BuildVPN")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Disconnect", pending)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(CHANNEL_ID, "VPN Connection", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
