package io.islandcn.app.shizuku

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

sealed class ShizukuState {
    object Unavailable : ShizukuState()
    object RunningNotAuthorized : ShizukuState()
    object Authorized : ShizukuState()
}

object ShizukuManager {
    private const val SHIZUKU_PERMISSION_REQUEST_CODE = 1001

    private val _state = MutableStateFlow<ShizukuState>(ShizukuState.Unavailable)
    val state: StateFlow<ShizukuState> = _state.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _state.value = ShizukuState.Unavailable
    }

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == SHIZUKU_PERMISSION_REQUEST_CODE) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    _state.value = ShizukuState.Authorized
                } else {
                    _state.value = ShizukuState.RunningNotAuthorized
                }
            }
        }

    fun init() {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
            checkStatus()
        } catch (_: Exception) {
            _state.value = ShizukuState.Unavailable
        }
    }

    fun checkStatus() {
        try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.isPreV11()) {
                    _state.value = ShizukuState.Unavailable
                } else if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    _state.value = ShizukuState.Authorized
                } else {
                    _state.value = ShizukuState.RunningNotAuthorized
                }
            } else {
                _state.value = ShizukuState.Unavailable
            }
        } catch (_: Exception) {
            _state.value = ShizukuState.Unavailable
        }
    }

    fun requestPermission() {
        try {
            if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
            }
        } catch (_: Exception) {
            // Ignored
        }
    }

    suspend fun executePrivilegedCommand(command: String): String = withContext(Dispatchers.IO) {
        if (_state.value != ShizukuState.Authorized) return@withContext "Error: Shizuku not authorized"

        try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as java.lang.Process

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            process.waitFor()
            output.toString().trim()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun autoGrantAllPermissions(context: Context): List<Pair<String, Boolean>> = withContext(Dispatchers.IO) {
        val results = mutableListOf<Pair<String, Boolean>>()
        val pkg = context.packageName

        // 1. Grant SYSTEM_ALERT_WINDOW via appops
        try {
            executePrivilegedCommand("appops set $pkg SYSTEM_ALERT_WINDOW allow")
            results.add("Overlay Window Permission (SYSTEM_ALERT_WINDOW)" to true)
        } catch (_: Exception) {
            results.add("Overlay Window Permission (SYSTEM_ALERT_WINDOW)" to false)
        }

        // 2. Grant Notification Listener access
        try {
            val serviceComponent = "$pkg/$pkg.service.IslandNotificationListener"
            executePrivilegedCommand("cmd notification set_notification_listener_access_granted --user cur $serviceComponent 1")
            results.add("Notification & Media Interceptor Access" to true)
        } catch (_: Exception) {
            results.add("Notification & Media Interceptor Access" to false)
        }

        // 3. Enable Accessibility Service automatically
        try {
            val accessibilityComponent = "$pkg/$pkg.service.IslandAccessibilityService"
            executePrivilegedCommand("settings put secure enabled_accessibility_services $accessibilityComponent")
            executePrivilegedCommand("settings put secure accessibility_enabled 1")
            results.add("IslandCN Accessibility Engine Activation" to true)
        } catch (_: Exception) {
            results.add("IslandCN Accessibility Engine Activation" to false)
        }

        results
    }
}
