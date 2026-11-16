package io.islandcn.app.data

import android.graphics.Bitmap

enum class CutoutPosition {
    CENTER,
    LEFT,
    RIGHT
}

enum class IslandExpansion {
    COMPACT,
    EXPANDED,
    MINIMAL_SPLIT
}

sealed class IslandEvent(val priority: Int) {
    data class Call(
        val callerName: String,
        val stateText: String = "Incoming Call",
        val durationSeconds: Int = 0
    ) : IslandEvent(priority = 100)

    data class Timer(
        val title: String = "Timer",
        val remainingSeconds: Int,
        val totalSeconds: Int,
        val isRunning: Boolean = true
    ) : IslandEvent(priority = 80)

    data class Battery(
        val percentage: Int,
        val isCharging: Boolean,
        val isFastCharging: Boolean = false,
        val wattageText: String = ""
    ) : IslandEvent(priority = 60)

    data class Notification(
        val id: String,
        val appName: String,
        val title: String,
        val text: String,
        val timestamp: Long = System.currentTimeMillis(),
        val packageName: String = ""
    ) : IslandEvent(priority = 50)

    data class Media(
        val title: String,
        val artist: String,
        val isPlaying: Boolean,
        val packageName: String = "",
        val durationMs: Long = 0,
        val positionMs: Long = 0,
        val albumArt: Bitmap? = null
    ) : IslandEvent(priority = 30)

    data class Alert(
        val title: String,
        val subtitle: String = "",
        val iconType: String = "info"
    ) : IslandEvent(priority = 40)
}

data class IslandConfig(
    val cutoutPosition: CutoutPosition = CutoutPosition.CENTER,
    val offsetX: Float = 0f,
    val offsetY: Float = 10f,
    val idleWidth: Float = 124f,
    val idleHeight: Float = 36f,
    val expandedWidth: Float = 350f,
    val expandedHeight: Float = 175f,
    val cornerRadius: Float = 24f,
    val hapticFeedback: Boolean = true,
    val ambientGlow: Boolean = true,
    val mediaEnabled: Boolean = true,
    val batteryEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val splitIslandEnabled: Boolean = true
)

data class IslandUiState(
    val activeEvent: IslandEvent? = null,
    val secondaryEvent: IslandEvent? = null,
    val expansion: IslandExpansion = IslandExpansion.COMPACT,
    val config: IslandConfig = IslandConfig()
)
