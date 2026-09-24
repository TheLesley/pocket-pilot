package com.example.pocketpilot.feature.security.domain.model

import androidx.compose.runtime.Immutable

/**
 * The three states the app-lock gate can be in. [Unknown] means the lock
 * manager hasn't produced its first evaluation yet, so the root UI should keep
 * showing the current screen instead of flickering to the lock screen on cold
 * start.
 */
@Immutable
sealed interface LockState {
    data object Unknown : LockState
    data object Unlocked : LockState
    data object Locked : LockState
}
