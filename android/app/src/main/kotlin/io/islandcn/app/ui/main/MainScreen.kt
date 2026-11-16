package io.islandcn.app.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import io.islandcn.app.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.islandcn.app.data.CutoutPosition
import io.islandcn.app.shizuku.ShizukuState
import io.islandcn.app.ui.components.IslandSurface
import io.islandcn.app.ui.theme.IslandBgDark
import io.islandcn.app.ui.theme.IslandCardBorder
import io.islandcn.app.ui.theme.IslandCardDark
import io.islandcn.app.ui.theme.NeonAmber
import io.islandcn.app.ui.theme.NeonCyan
import io.islandcn.app.ui.theme.NeonGreen
import io.islandcn.app.ui.theme.NeonIndigo
import io.islandcn.app.ui.theme.NeonRose
import io.islandcn.app.ui.theme.NeonViolet
import io.islandcn.app.ui.theme.OledBlack
import io.islandcn.app.ui.theme.TextPrimary
import io.islandcn.app.ui.theme.TextSecondary
import io.islandcn.app.ui.theme.TextTertiary

@Composable
fun MainScreen(viewModel: MainScreenViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto refresh permissions when returning to foreground
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val uiState by viewModel.islandUiState.collectAsState()
    val preset by viewModel.selectedPreset.collectAsState()
    val permissions by viewModel.permissionStatus.collectAsState()
    val autoGrantResult by viewModel.autoGrantResult.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IslandBgDark)
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        // App Header & Brand
        HeaderSection(
            permissions = permissions,
            onRefresh = { viewModel.refreshStatus() }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Hero: Interactive Island Simulator
        SimulatorSandbox(
            uiState = uiState,
            currentPreset = preset,
            onPresetSelect = { viewModel.selectPreset(it) },
            onToggleExpand = { viewModel.toggleSimulatorExpansion() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dual Engine Activation Matrix (Accessibility + Shizuku)
        DualEngineSection(
            permissions = permissions,
            autoGrantResult = autoGrantResult,
            onOpenAccessibility = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            onRequestShizuku = { viewModel.requestShizukuPermission() },
            onAutoGrantShizuku = { viewModel.autoGrantViaShizuku() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Overlay Service Master Switch
        OverlayServiceSection(
            permissions = permissions,
            onToggleService = { viewModel.toggleOverlayService() },
            onOpenOverlaySettings = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    context.startActivity(intent)
                }
            },
            onOpenNotificationSettings = {
                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Camera Cutout Calibration Studio
        CalibrationStudioSection(
            config = uiState.config,
            onUpdateConfig = { viewModel.updateConfig(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Live Event Fire Board (Test over punch-hole right now)
        QuickFireBoardSection(
            onFire = { viewModel.selectPreset(it) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Customization & Feature Toggles
        FeatureTogglesSection(
            config = uiState.config,
            onUpdateConfig = { viewModel.updateConfig(it) }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun HeaderSection(
    permissions: PermissionStatus,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Your Island Logo",
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, NeonIndigo.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "YOUR",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "ISLAND",
                        color = NeonIndigo,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonIndigo.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "v1.0",
                            color = NeonIndigo,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Dynamic Island for Android",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(IslandCardDark)
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = "Refresh Status",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SimulatorSandbox(
    uiState: io.islandcn.app.data.IslandUiState,
    currentPreset: SimulatorPreset,
    onPresetSelect: (SimulatorPreset) -> Unit,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, IslandCardBorder, RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = IslandCardDark),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE INTERACTIVE SANDBOX",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Text(
                    text = "Tap Island to Expand",
                    color = NeonIndigo,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Simulated Phone Screen Cutout Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF030305))
                    .border(1.dp, Color(0xFF1E2030), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.TopCenter
            ) {
                // Background subtle ambient grid/gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF111422).copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Simulated punch hole & floating island
                Box(modifier = Modifier.padding(top = 16.dp)) {
                    IslandSurface(
                        uiState = uiState,
                        onToggleExpand = onToggleExpand,
                        onDismissEvent = { onPresetSelect(SimulatorPreset.IDLE) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Preset Switchers Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PresetPill(
                    icon = Icons.Rounded.MusicNote,
                    label = "Music",
                    selected = currentPreset == SimulatorPreset.MUSIC,
                    activeColor = NeonViolet,
                    onClick = { onPresetSelect(SimulatorPreset.MUSIC) },
                    modifier = Modifier.weight(1f)
                )
                PresetPill(
                    icon = Icons.Rounded.Bolt,
                    label = "Charge",
                    selected = currentPreset == SimulatorPreset.BATTERY,
                    activeColor = NeonGreen,
                    onClick = { onPresetSelect(SimulatorPreset.BATTERY) },
                    modifier = Modifier.weight(1f)
                )
                PresetPill(
                    icon = Icons.Rounded.Notifications,
                    label = "Alert",
                    selected = currentPreset == SimulatorPreset.NOTIFICATION,
                    activeColor = NeonCyan,
                    onClick = { onPresetSelect(SimulatorPreset.NOTIFICATION) },
                    modifier = Modifier.weight(1f)
                )
                PresetPill(
                    icon = Icons.Rounded.Call,
                    label = "Call",
                    selected = currentPreset == SimulatorPreset.CALL,
                    activeColor = NeonRose,
                    onClick = { onPresetSelect(SimulatorPreset.CALL) },
                    modifier = Modifier.weight(1f)
                )
                PresetPill(
                    icon = Icons.Rounded.Timer,
                    label = "Timer",
                    selected = currentPreset == SimulatorPreset.TIMER,
                    activeColor = NeonAmber,
                    onClick = { onPresetSelect(SimulatorPreset.TIMER) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PresetPill(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) activeColor.copy(alpha = 0.22f) else Color(0xFF181926))
            .border(
                1.dp,
                if (selected) activeColor.copy(alpha = 0.6f) else Color(0xFF26283B),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) activeColor else TextSecondary,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = if (selected) TextPrimary else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DualEngineSection(
    permissions: PermissionStatus,
    autoGrantResult: String?,
    onOpenAccessibility: () -> Unit,
    onRequestShizuku: () -> Unit,
    onAutoGrantShizuku: () -> Unit
) {
    Column {
        Text(
            text = "DUAL OPERATING ENGINES",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        // 1. Accessibility Service Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (permissions.isAccessibilityActive) NeonGreen.copy(alpha = 0.5f) else IslandCardBorder,
                    RoundedCornerShape(18.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = IslandCardDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccessibilityNew,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Accessibility Engine",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Standard Android Accessibility Layer",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    StatusBadge(
                        active = permissions.isAccessibilityActive,
                        activeText = "ACTIVE",
                        inactiveText = "DISABLED",
                        activeColor = NeonGreen
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Runs without PC or root. Enables drawing dynamic overlays over punch-holes and tracks active app switches.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onOpenAccessibility,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (permissions.isAccessibilityActive) Color(0xFF1E2822) else NeonGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (permissions.isAccessibilityActive) "Accessibility Engine Configured ✓" else "Enable in Accessibility Settings",
                        color = if (permissions.isAccessibilityActive) NeonGreen else Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Shizuku Elevated Engine Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    if (permissions.shizukuState is ShizukuState.Authorized) NeonCyan.copy(alpha = 0.5f) else IslandCardBorder,
                    RoundedCornerShape(18.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = IslandCardDark),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Terminal,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Shizuku Privileged Engine",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Elevated Wireless ADB Bridge",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    when (permissions.shizukuState) {
                        is ShizukuState.Authorized -> StatusBadge(
                            active = true,
                            activeText = "ELEVATED",
                            inactiveText = "",
                            activeColor = NeonCyan
                        )
                        is ShizukuState.RunningNotAuthorized -> StatusBadge(
                            active = false,
                            activeText = "",
                            inactiveText = "AUTH NEEDED",
                            activeColor = NeonAmber
                        )
                        is ShizukuState.Unavailable -> StatusBadge(
                            active = false,
                            activeText = "",
                            inactiveText = "OFFLINE",
                            activeColor = TextTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Allows executing privileged shell commands to auto-grant overlay, notification access, and system toggles in 1 tap without PC.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (permissions.shizukuState) {
                    is ShizukuState.Authorized -> {
                        Button(
                            onClick = onAutoGrantShizuku,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "⚡ Auto-Grant All Permissions via Shizuku",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                    is ShizukuState.RunningNotAuthorized -> {
                        Button(
                            onClick = onRequestShizuku,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonAmber),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Authorize Shizuku Access",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                    is ShizukuState.Unavailable -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1B1C2A))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Shizuku is not running on this device. You can still use the Accessibility Engine above freely!",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (!autoGrantResult.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F1B18))
                            .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = autoGrantResult,
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayServiceSection(
    permissions: PermissionStatus,
    onToggleService: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, IslandCardBorder, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = IslandCardDark),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonIndigo.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Layers,
                            contentDescription = null,
                            tint = NeonIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Direct Overlay Service",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Draws directly on screen",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Switch(
                    checked = permissions.isOverlayServiceRunning || permissions.isAccessibilityActive,
                    onCheckedChange = { onToggleService() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = NeonIndigo,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = Color(0xFF26283B)
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-permission badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PermissionMiniChip(
                    label = "Draw Overlay",
                    granted = permissions.hasOverlayPermission,
                    onClick = onOpenOverlaySettings,
                    modifier = Modifier.weight(1f)
                )
                PermissionMiniChip(
                    label = "Notification Hub",
                    granted = permissions.hasNotificationPermission,
                    onClick = onOpenNotificationSettings,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PermissionMiniChip(
    label: String,
    granted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (granted) NeonGreen.copy(alpha = 0.12f) else NeonRose.copy(alpha = 0.12f))
            .border(
                1.dp,
                if (granted) NeonGreen.copy(alpha = 0.4f) else NeonRose.copy(alpha = 0.4f),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (granted) "✓" else "Grant",
                color = if (granted) NeonGreen else NeonRose,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CalibrationStudioSection(
    config: io.islandcn.app.data.IslandConfig,
    onUpdateConfig: ((io.islandcn.app.data.IslandConfig) -> io.islandcn.app.data.IslandConfig) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, IslandCardBorder, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = IslandCardDark),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonViolet.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        tint = NeonViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Cutout Calibration Studio",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Align Island with your camera punch hole",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cutout position segmented switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CutoutSegmentButton(
                    label = "Left Hole",
                    selected = config.cutoutPosition == CutoutPosition.LEFT,
                    onClick = { onUpdateConfig { it.copy(cutoutPosition = CutoutPosition.LEFT) } },
                    modifier = Modifier.weight(1f)
                )
                CutoutSegmentButton(
                    label = "Center Hole",
                    selected = config.cutoutPosition == CutoutPosition.CENTER,
                    onClick = { onUpdateConfig { it.copy(cutoutPosition = CutoutPosition.CENTER) } },
                    modifier = Modifier.weight(1f)
                )
                CutoutSegmentButton(
                    label = "Right Hole",
                    selected = config.cutoutPosition == CutoutPosition.RIGHT,
                    onClick = { onUpdateConfig { it.copy(cutoutPosition = CutoutPosition.RIGHT) } },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // X Offset Slider
            SliderRow(
                title = "Horizontal Offset (X)",
                valueText = "${config.offsetX.toInt()} dp",
                value = config.offsetX,
                range = -100f..100f,
                onValueChange = { onUpdateConfig { c -> c.copy(offsetX = it) } }
            )

            // Y Offset Slider
            SliderRow(
                title = "Vertical Position (Y)",
                valueText = "${config.offsetY.toInt()} dp",
                value = config.offsetY,
                range = 0f..60f,
                onValueChange = { onUpdateConfig { c -> c.copy(offsetY = it) } }
            )

            // Idle Width Slider
            SliderRow(
                title = "Idle Pill Width",
                valueText = "${config.idleWidth.toInt()} dp",
                value = config.idleWidth,
                range = 90f..180f,
                onValueChange = { onUpdateConfig { c -> c.copy(idleWidth = it) } }
            )

            // Idle Height Slider
            SliderRow(
                title = "Idle Pill Height",
                valueText = "${config.idleHeight.toInt()} dp",
                value = config.idleHeight,
                range = 28f..48f,
                onValueChange = { onUpdateConfig { c -> c.copy(idleHeight = it) } }
            )
        }
    }
}

@Composable
private fun CutoutSegmentButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) NeonViolet.copy(alpha = 0.2f) else Color(0xFF1B1C2A))
            .border(
                1.dp,
                if (selected) NeonViolet.copy(alpha = 0.6f) else Color(0xFF2B2D40),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) TextPrimary else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun SliderRow(
    title: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, color = TextSecondary, fontSize = 12.sp)
            Text(text = valueText, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = NeonViolet,
                activeTrackColor = NeonViolet,
                inactiveTrackColor = Color(0xFF26283B)
            )
        )
    }
}

@Composable
private fun QuickFireBoardSection(
    onFire: (SimulatorPreset) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, IslandCardBorder, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = IslandCardDark),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "REAL-TIME SCREEN EVENT TRIGGER",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "Dispatch live animations immediately to your floating island",
                color = TextTertiary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickFireButton(
                    label = "🎵 Music Event",
                    color = NeonViolet,
                    onClick = { onFire(SimulatorPreset.MUSIC) },
                    modifier = Modifier.weight(1f)
                )
                QuickFireButton(
                    label = "⚡ 65W Flash Charge",
                    color = NeonGreen,
                    onClick = { onFire(SimulatorPreset.BATTERY) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickFireButton(
                    label = "💬 WhatsApp Ping",
                    color = NeonCyan,
                    onClick = { onFire(SimulatorPreset.NOTIFICATION) },
                    modifier = Modifier.weight(1f)
                )
                QuickFireButton(
                    label = "📞 Incoming Call",
                    color = NeonRose,
                    onClick = { onFire(SimulatorPreset.CALL) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickFireButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FeatureTogglesSection(
    config: io.islandcn.app.data.IslandConfig,
    onUpdateConfig: ((io.islandcn.app.data.IslandConfig) -> io.islandcn.app.data.IslandConfig) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, IslandCardBorder, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = IslandCardDark),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ENGINE PREFERENCES",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            ToggleRow(
                title = "Ambient Atmospheric Glow",
                subtitle = "Radiates soft colored light around the island surface",
                checked = config.ambientGlow,
                onCheckedChange = { onUpdateConfig { c -> c.copy(ambientGlow = it) } }
            )

            ToggleRow(
                title = "Split Island for Multi-Tasking",
                subtitle = "Separates into dual bubbles when music and timer co-exist",
                checked = config.splitIslandEnabled,
                onCheckedChange = { onUpdateConfig { c -> c.copy(splitIslandEnabled = it) } }
            )

            ToggleRow(
                title = "Tactile Haptic Feedback",
                subtitle = "Subtle vibration when island expands or contracts",
                checked = config.hapticFeedback,
                onCheckedChange = { onUpdateConfig { c -> c.copy(hapticFeedback = it) } }
            )
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NeonIndigo,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = Color(0xFF26283B)
            )
        )
    }
}

@Composable
private fun StatusBadge(
    active: Boolean,
    activeText: String,
    inactiveText: String,
    activeColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) activeColor.copy(alpha = 0.15f) else Color(0xFF26283B))
            .border(
                1.dp,
                if (active) activeColor.copy(alpha = 0.5f) else Color(0xFF373950),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (active) activeText else inactiveText,
            color = if (active) activeColor else TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
