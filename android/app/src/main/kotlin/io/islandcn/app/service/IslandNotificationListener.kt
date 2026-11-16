package io.islandcn.app.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import io.islandcn.app.data.IslandEvent
import io.islandcn.app.data.IslandStateRepository

class IslandNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        isRunning = true
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isRunning = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // 1. Check for Media Playback Session
        val isMedia = extras.containsKey(Notification.EXTRA_MEDIA_SESSION) ||
                notification.category == Notification.CATEGORY_TRANSPORT

        if (isMedia) {
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val artist = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

            if (title.isNotBlank()) {
                IslandStateRepository.postMedia(
                    IslandEvent.Media(
                        title = title,
                        artist = artist,
                        isPlaying = true,
                        packageName = sbn.packageName
                    )
                )
            }
            return
        }

        // 2. Regular Notification / Message
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val pkg = sbn.packageName ?: ""

        if (title.isNotBlank() && !sbn.isOngoing) {
            val appLabel = try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                pkg.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            }

            IslandStateRepository.postNotification(
                IslandEvent.Notification(
                    id = sbn.key ?: System.currentTimeMillis().toString(),
                    appName = appLabel,
                    title = title,
                    text = text,
                    packageName = pkg
                )
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val notification = sbn.notification ?: return
        val isMedia = notification.extras?.containsKey(Notification.EXTRA_MEDIA_SESSION) == true ||
                notification.category == Notification.CATEGORY_TRANSPORT

        if (isMedia) {
            // Dismiss or pause media event
            IslandStateRepository.postMedia(null)
        }
    }

    companion object {
        var isRunning: Boolean = false
            private set
    }
}
