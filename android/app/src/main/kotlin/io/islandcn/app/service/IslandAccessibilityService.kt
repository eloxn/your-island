package io.islandcn.app.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.view.accessibility.AccessibilityEvent
import io.islandcn.app.data.IslandEvent
import io.islandcn.app.data.IslandStateRepository

class IslandAccessibilityService : AccessibilityService() {

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

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true

        overlayManager = IslandOverlayManager(this, isAccessibility = true).apply {
            show()
        }

        // Register power receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(batteryReceiver, filter)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        when (event.eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                val texts = event.text
                if (texts.isNotEmpty()) {
                    val title = texts.firstOrNull()?.toString() ?: ""
                    val subtitle = if (texts.size > 1) texts[1].toString() else ""
                    val pkg = event.packageName?.toString() ?: ""

                    if (title.isNotBlank()) {
                        IslandStateRepository.postNotification(
                            IslandEvent.Notification(
                                id = System.currentTimeMillis().toString(),
                                appName = pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() },
                                title = title,
                                text = subtitle,
                                packageName = pkg
                            )
                        )
                    }
                }
            }
            else -> {
                // Window state / foreground transitions
            }
        }
    }

    override fun onInterrupt() {
        // Accessibility service interrupted
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

    companion object {
        var isRunning: Boolean = false
            private set
    }
}
