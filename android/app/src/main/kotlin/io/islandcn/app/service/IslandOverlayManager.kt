package io.islandcn.app.service

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import io.islandcn.app.MainActivity
import io.islandcn.app.data.CutoutPosition
import io.islandcn.app.data.IslandExpansion
import io.islandcn.app.data.IslandStateRepository
import io.islandcn.app.ui.components.IslandSurface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class IslandOverlayManager(private val context: Context, private val isAccessibility: Boolean = false) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private var scope: CoroutineScope? = null
    private var isOverlayAttached = false

    fun show() {
        if (isOverlayAttached) return

        val owner = OverlayLifecycleOwner()
        lifecycleOwner = owner
        owner.start()

        val view = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)

            setContent {
                val uiState by IslandStateRepository.uiState.collectAsState()
                IslandSurface(
                    uiState = uiState,
                    onToggleExpand = {
                        IslandStateRepository.toggleExpansion()
                    },
                    onDismissEvent = {
                        IslandStateRepository.dismissCurrent()
                    },
                    onLongPress = {
                        val intent = Intent(context, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
        composeView = view

        val layoutParams = createLayoutParams()
        try {
            windowManager.addView(view, layoutParams)
            isOverlayAttached = true

            // Observe config and expansion changes to adjust WindowManager params
            val job = Job()
            val coroutineScope = CoroutineScope(Dispatchers.Main + job)
            scope = coroutineScope

            coroutineScope.launch {
                IslandStateRepository.uiState.collectLatest { state ->
                    updateLayoutParams(state)
                }
            }
        } catch (_: Exception) {
            isOverlayAttached = false
        }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val windowType = if (isAccessibility) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
        }

        val config = IslandStateRepository.uiState.value.config
        val gravity = when (config.cutoutPosition) {
            CutoutPosition.LEFT -> Gravity.TOP or Gravity.START
            CutoutPosition.RIGHT -> Gravity.TOP or Gravity.END
            CutoutPosition.CENTER -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
        }

        val density = context.resources.displayMetrics.density
        val xPos = (config.offsetX * density).toInt()
        val yPos = (config.offsetY * density).toInt()

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = gravity
            this.x = xPos
            this.y = yPos
        }
    }

    private fun updateLayoutParams(state: io.islandcn.app.data.IslandUiState) {
        val view = composeView ?: return
        if (!isOverlayAttached) return

        try {
            val params = view.layoutParams as? WindowManager.LayoutParams ?: return
            val config = state.config
            val density = context.resources.displayMetrics.density

            val gravity = when (config.cutoutPosition) {
                CutoutPosition.LEFT -> Gravity.TOP or Gravity.START
                CutoutPosition.RIGHT -> Gravity.TOP or Gravity.END
                CutoutPosition.CENTER -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
            }

            params.gravity = gravity
            params.x = (config.offsetX * density).toInt()
            params.y = (config.offsetY * density).toInt()

            windowManager.updateViewLayout(view, params)
        } catch (_: Exception) {
            // Ignored
        }
    }

    fun hide() {
        if (!isOverlayAttached) return
        try {
            composeView?.let { windowManager.removeView(it) }
        } catch (_: Exception) {
            // Ignored
        } finally {
            isOverlayAttached = false
            composeView = null
            lifecycleOwner?.stop()
            lifecycleOwner = null
        }
    }
}
