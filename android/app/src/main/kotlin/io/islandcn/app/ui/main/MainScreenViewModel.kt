package io.islandcn.app.ui.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.islandcn.app.data.CutoutPosition
import io.islandcn.app.data.IslandConfig
import io.islandcn.app.data.IslandEvent
import io.islandcn.app.data.IslandExpansion
import io.islandcn.app.data.IslandPreferences
import io.islandcn.app.data.IslandStateRepository
import io.islandcn.app.data.IslandUiState
import io.islandcn.app.service.IslandAccessibilityService
import io.islandcn.app.service.IslandNotificationListener
import io.islandcn.app.service.IslandOverlayService
import io.islandcn.app.shizuku.ShizukuManager
import io.islandcn.app.shizuku.ShizukuState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SimulatorPreset {
    IDLE,
    MUSIC,
    BATTERY,
    NOTIFICATION,
    CALL,
    TIMER
}

data class PermissionStatus(
    val hasOverlayPermission: Boolean,
    val hasNotificationPermission: Boolean,
    val isAccessibilityActive: Boolean,
    val isOverlayServiceRunning: Boolean,
    val shizukuState: ShizukuState
)

class MainScreenViewModel(private val context: Context) : ViewModel() {
    private val preferences = IslandPreferences(context)

    val islandUiState: StateFlow<IslandUiState> = IslandStateRepository.uiState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IslandUiState())

    private val _selectedPreset = MutableStateFlow(SimulatorPreset.MUSIC)
    val selectedPreset: StateFlow<SimulatorPreset> = _selectedPreset.asStateFlow()

    private val _permissionStatus = MutableStateFlow(checkPermissions())
    val permissionStatus: StateFlow<PermissionStatus> = _permissionStatus.asStateFlow()

    private val _autoGrantResult = MutableStateFlow<String?>(null)
    val autoGrantResult: StateFlow<String?> = _autoGrantResult.asStateFlow()

    init {
        // Synchronize repository with saved preferences
        viewModelScope.launch {
            preferences.configFlow.collect { config ->
                IslandStateRepository.setConfig(config)
            }
        }

        // Initialize Shizuku manager
        ShizukuManager.init()

        // Set initial simulator state
        selectPreset(SimulatorPreset.MUSIC)
    }

    fun refreshStatus() {
        ShizukuManager.checkStatus()
        _permissionStatus.value = checkPermissions()
    }

    private fun checkPermissions(): PermissionStatus {
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true

        return PermissionStatus(
            hasOverlayPermission = hasOverlay,
            hasNotificationPermission = IslandNotificationListener.isRunning,
            isAccessibilityActive = IslandAccessibilityService.isRunning,
            isOverlayServiceRunning = IslandOverlayService.isRunning,
            shizukuState = ShizukuManager.state.value
        )
    }

    fun selectPreset(preset: SimulatorPreset) {
        _selectedPreset.value = preset
        when (preset) {
            SimulatorPreset.IDLE -> {
                IslandStateRepository.postMedia(null)
                IslandStateRepository.postCall(null)
                IslandStateRepository.postTimer(null)
            }
            SimulatorPreset.MUSIC -> {
                IslandStateRepository.postMedia(
                    IslandEvent.Media(
                        title = "Blinding Lights",
                        artist = "The Weeknd • After Hours",
                        isPlaying = true,
                        packageName = "com.spotify.music"
                    )
                )
            }
            SimulatorPreset.BATTERY -> {
                IslandStateRepository.postBattery(
                    IslandEvent.Battery(
                        percentage = 88,
                        isCharging = true,
                        isFastCharging = true,
                        wattageText = "65W SuperVOOC Flash Charge"
                    ),
                    durationMs = 15000L
                )
            }
            SimulatorPreset.NOTIFICATION -> {
                IslandStateRepository.postNotification(
                    IslandEvent.Notification(
                        id = "sim_notif",
                        appName = "WhatsApp",
                        title = "Elena Rostova",
                        text = "The prototype APK looks gorgeous! 🚀",
                        packageName = "com.whatsapp"
                    ),
                    durationMs = 15000L
                )
            }
            SimulatorPreset.CALL -> {
                IslandStateRepository.postCall(
                    IslandEvent.Call(
                        callerName = "Design Team Hub",
                        stateText = "00:42 • Active Call"
                    )
                )
            }
            SimulatorPreset.TIMER -> {
                IslandStateRepository.postTimer(
                    IslandEvent.Timer(
                        title = "Focus Timer",
                        remainingSeconds = 14 * 60 + 35,
                        totalSeconds = 25 * 60,
                        isRunning = true
                    )
                )
            }
        }
    }

    fun toggleSimulatorExpansion() {
        IslandStateRepository.toggleExpansion()
    }

    fun updateConfig(update: (IslandConfig) -> IslandConfig) {
        preferences.updateConfig(update)
    }

    fun requestShizukuPermission() {
        ShizukuManager.requestPermission()
        refreshStatus()
    }

    fun autoGrantViaShizuku() {
        viewModelScope.launch {
            _autoGrantResult.value = "Executing privileged ADB shell configuration..."
            val results = ShizukuManager.autoGrantAllPermissions(context)
            val summary = results.joinToString("\n") { (name, ok) ->
                "${if (ok) "✓" else "✗"} $name"
            }
            _autoGrantResult.value = summary
            refreshStatus()
        }
    }

    fun toggleOverlayService() {
        if (IslandOverlayService.isRunning) {
            context.stopService(Intent(context, IslandOverlayService::class.java))
        } else {
            val intent = Intent(context, IslandOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        refreshStatus()
    }
}
