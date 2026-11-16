package io.islandcn.app.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object IslandStateRepository {
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _uiState = MutableStateFlow(IslandUiState())
    val uiState: StateFlow<IslandUiState> = _uiState.asStateFlow()

    private var activeMediaEvent: IslandEvent.Media? = null
    private var activeCallEvent: IslandEvent.Call? = null
    private var activeTimerEvent: IslandEvent.Timer? = null

    private var autoDismissJob: Job? = null

    fun setConfig(config: IslandConfig) {
        _uiState.value = _uiState.value.copy(config = config)
    }

    fun toggleExpansion() {
        val current = _uiState.value.expansion
        _uiState.value = _uiState.value.copy(
            expansion = if (current == IslandExpansion.EXPANDED) IslandExpansion.COMPACT else IslandExpansion.EXPANDED
        )
    }

    fun setExpansion(expansion: IslandExpansion) {
        _uiState.value = _uiState.value.copy(expansion = expansion)
    }

    fun postNotification(event: IslandEvent.Notification, durationMs: Long = 4500L) {
        if (!_uiState.value.config.notificationsEnabled) return
        displayTransientEvent(event, durationMs)
    }

    fun postBattery(event: IslandEvent.Battery, durationMs: Long = 4000L) {
        if (!_uiState.value.config.batteryEnabled) return
        displayTransientEvent(event, durationMs)
    }

    fun postAlert(event: IslandEvent.Alert, durationMs: Long = 3000L) {
        displayTransientEvent(event, durationMs)
    }

    fun postMedia(event: IslandEvent.Media?) {
        if (!_uiState.value.config.mediaEnabled) {
            activeMediaEvent = null
            recalculateState()
            return
        }
        activeMediaEvent = if (event != null && (event.isPlaying || event.title.isNotBlank())) event else null
        recalculateState()
    }

    fun postCall(event: IslandEvent.Call?) {
        activeCallEvent = event
        recalculateState()
    }

    fun postTimer(event: IslandEvent.Timer?) {
        activeTimerEvent = event
        recalculateState()
    }

    fun dismissCurrent() {
        autoDismissJob?.cancel()
        recalculateState()
    }

    private fun displayTransientEvent(event: IslandEvent, durationMs: Long) {
        autoDismissJob?.cancel()
        _uiState.value = _uiState.value.copy(
            activeEvent = event,
            secondaryEvent = if (_uiState.value.config.splitIslandEnabled) activeMediaEvent else null
        )
        autoDismissJob = scope.launch {
            delay(durationMs)
            recalculateState()
        }
    }

    private fun recalculateState() {
        // Priority hierarchy: Call > Timer > Active Media > Idle
        val primary = activeCallEvent ?: activeTimerEvent ?: activeMediaEvent
        val secondary = if (_uiState.value.config.splitIslandEnabled && activeTimerEvent != null && activeMediaEvent != null) {
            activeMediaEvent
        } else {
            null
        }

        _uiState.value = _uiState.value.copy(
            activeEvent = primary,
            secondaryEvent = secondary,
            expansion = if (primary == null) IslandExpansion.COMPACT else _uiState.value.expansion
        )
    }
}
