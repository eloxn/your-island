package io.islandcn.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.islandcn.app.MainActivity
import io.islandcn.app.data.IslandEvent
import io.islandcn.app.data.IslandStateRepository

class IslandOverlayService : Service() {

    private var overlayManager: IslandOverlayManager? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    val batteryIntent = context?.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 50
                    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
                    val pct = (level * 100 / scale.toFloat()).toInt()

                    val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: 0
                    val isFast = plugged == BatteryManager.BATTERY_PLUGGED_AC

                    IslandStateRepository.postBattery(
                        IslandEvent.Battery(
                            percentage = pct,
                            isCharging = true,
                            isFastCharging = isFast,
                            wattageText = if (isFast) "65W Super Flash Charge" else "Standard Charging"
                        )
                    )
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    IslandStateRepository.postAlert(
                        IslandEvent.Alert(
                            title = "Power Disconnected",
                            subtitle = "Running on battery"
                        )
                    )
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        startForegroundNotification()

        overlayManager = IslandOverlayManager(this, isAccessibility = false).apply {
            show()
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(batteryReceiver, filter)
    }

    private fun startForegroundNotification() {
        val channelId = "islandcn_overlay_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "IslandCN Floating Island",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps IslandCN Dynamic Island active over your camera cutout"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("IslandCN Active")
            .setContentText("Dynamic Island is hovering over your camera cutout")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(101, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try {
            unregisterReceiver(batteryReceiver)
        } catch (_: Exception) {
            // Ignored
        }
        overlayManager?.hide()
        overlayManager = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        var isRunning: Boolean = false
            private set
    }
}
