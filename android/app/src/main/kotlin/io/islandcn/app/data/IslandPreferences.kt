package io.islandcn.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IslandPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("islandcn_prefs", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<IslandConfig> = _configFlow.asStateFlow()

    private fun loadConfig(): IslandConfig {
        val positionStr = prefs.getString("cutout_position", CutoutPosition.CENTER.name)
        val position = try {
            CutoutPosition.valueOf(positionStr ?: CutoutPosition.CENTER.name)
        } catch (_: Exception) {
            CutoutPosition.CENTER
        }

        return IslandConfig(
            cutoutPosition = position,
            offsetX = prefs.getFloat("offset_x", 0f),
            offsetY = prefs.getFloat("offset_y", 10f),
            idleWidth = prefs.getFloat("idle_width", 124f),
            idleHeight = prefs.getFloat("idle_height", 36f),
            expandedWidth = prefs.getFloat("expanded_width", 350f),
            expandedHeight = prefs.getFloat("expanded_height", 175f),
            cornerRadius = prefs.getFloat("corner_radius", 24f),
            hapticFeedback = prefs.getBoolean("haptic_feedback", true),
            ambientGlow = prefs.getBoolean("ambient_glow", true),
            mediaEnabled = prefs.getBoolean("media_enabled", true),
            batteryEnabled = prefs.getBoolean("battery_enabled", true),
            notificationsEnabled = prefs.getBoolean("notifications_enabled", true),
            splitIslandEnabled = prefs.getBoolean("split_island_enabled", true)
        )
    }

    fun updateConfig(update: (IslandConfig) -> IslandConfig) {
        val current = _configFlow.value
        val updated = update(current)
        prefs.edit()
            .putString("cutout_position", updated.cutoutPosition.name)
            .putFloat("offset_x", updated.offsetX)
            .putFloat("offset_y", updated.offsetY)
            .putFloat("idle_width", updated.idleWidth)
            .putFloat("idle_height", updated.idleHeight)
            .putFloat("expanded_width", updated.expandedWidth)
            .putFloat("expanded_height", updated.expandedHeight)
            .putFloat("corner_radius", updated.cornerRadius)
            .putBoolean("haptic_feedback", updated.hapticFeedback)
            .putBoolean("ambient_glow", updated.ambientGlow)
            .putBoolean("media_enabled", updated.mediaEnabled)
            .putBoolean("battery_enabled", updated.batteryEnabled)
            .putBoolean("notifications_enabled", updated.notificationsEnabled)
            .putBoolean("split_island_enabled", updated.splitIslandEnabled)
            .apply()
        _configFlow.value = updated
    }
}
