package com.example.pocketpilot.feature.notifications.data.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Helpers around the runtime `POST_NOTIFICATIONS` permission introduced in
 * Android 13 (API 33). Older releases grant notifications implicitly, so
 * these helpers return `true` there and the UI skips the prompt.
 *
 * Kept in the data layer beside the dispatcher so the ViewModel only has to
 * observe `NotificationDispatcher.areNotificationsAllowed()` — the actual
 * permission plumbing sits in the Activity that owns the `ActivityResultLauncher`.
 */
object NotificationPermission {

    /** Whether the API-33 runtime permission is even required on this device. */
    val isRuntimePermissionRequired: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun isGranted(context: Context): Boolean {
        if (!isRuntimePermissionRequired) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** The permission string, or `null` on pre-API-33 where no runtime prompt is needed. */
    val permission: String?
        get() = if (isRuntimePermissionRequired) Manifest.permission.POST_NOTIFICATIONS else null
}
