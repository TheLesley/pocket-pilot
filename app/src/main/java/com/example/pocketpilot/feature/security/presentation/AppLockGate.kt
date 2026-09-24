package com.example.pocketpilot.feature.security.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocketpilot.feature.security.di.SecurityContainer
import com.example.pocketpilot.feature.security.di.SecurityViewModelFactory
import com.example.pocketpilot.feature.security.domain.model.LockState

/**
 * Gate composable that either shows the [LockScreen] or the [content] behind
 * it. Backed by [AppLockManager.state] so lifecycle transitions in
 * [AppLockManager] transparently swap the UI without any Activity-level
 * plumbing.
 */
@Composable
fun AppLockGate(content: @Composable () -> Unit) {
    val manager = remember { SecurityContainer.appLockManager }
    val state by manager.state.collectAsStateWithLifecycle()

    when (state) {
        LockState.Unknown,
        LockState.Unlocked -> content()

        LockState.Locked -> {
            val viewModel: LockViewModel = viewModel(
                key = "security.lock",
                factory = remember { SecurityViewModelFactory() }
            )
            LockRoute(
                viewModel = viewModel,
                onUnlocked = manager::markUnlocked
            )
        }
    }
}
