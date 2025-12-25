package com.buildvpn.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat

class VpnService : VpnService() {
    
    private var vpnInterface: ParcelFileDescriptor? = null
    
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel("vpn", "VPN", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {
                vpnInterface = Builder()
                    .setSession("BuildVPN")
                    .addAddress("10.0.0.2", 32)
                    .addRoute("0.0.0.0", 0)
                    .addDnsServer("8.8.8.8")
                    .establish()
                
                val notification = NotificationCompat.Builder(this, "vpn")
                    .setContentTitle("BuildVPN Active")
                    .setContentText("VPN is connected")
                    .setSmallIcon(android.R.drawable.ic_lock_lock)
                    .setContentIntent(PendingIntent.getActivity(this, 0, 
                        Intent(this, MainActivity::class.java), 
                        PendingIntent.FLAG_IMMUTABLE))
                    .build()
                
                startForeground(1, notification)
            }
            "STOP" -> {
                vpnInterface?.close()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }
}
