package com.example.pocketpilot.feature.security.presentation

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.pocketpilot.feature.security.domain.model.AutoLockTimeout
import com.example.pocketpilot.feature.security.domain.model.LockState
import com.example.pocketpilot.feature.security.domain.model.SecurityPreferences
import com.example.pocketpilot.feature.security.domain.usecase.ObserveSecurityPreferencesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Process-lifecycle observer that decides when the app should be locked.
 *
 * Rules:
 *  - If the user has neither biometric nor PIN configured, the app is always
 *    unlocked.
 *  - On the very first foreground event of the process we lock — the app is
 *    considered "cold" and must re-authenticate.
 *  - On subsequent foreground events we lock when the elapsed background time
 *    is greater than or equal to the configured [AutoLockTimeout], with
 *    [AutoLockTimeout.NEVER] treated as "keep the current state".
 *
 * The manager exposes a [state] StateFlow so the compose gate can react
 * without needing its own timer.
 */
class AppLockManager(
    private val observeSecurityPreferences: ObserveSecurityPreferencesUseCase,
    private val processLifecycleOwner: LifecycleOwner = ProcessLifecycleOwner.get(),
    private val timeSource: TimeSource = TimeSource.Monotonic
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow<LockState>(LockState.Unknown)
    val state: StateFlow<LockState> = _state.asStateFlow()

    @Volatile
    private var preferences: SecurityPreferences = SecurityPreferences.Default

    @Volatile
    private var backgroundedAt: TimeMark? = null

    @Volatile
    private var hasEverForegrounded: Boolean = false

    fun start() {
        processLifecycleOwner.lifecycle.addObserver(this)
        observeSecurityPreferences()
            .onEach { prefs ->
                preferences = prefs
                if (!prefs.isLockEnabled && _state.value == LockState.Locked) {
                    // User disabled all lock methods while the screen was up;
                    // release the gate so they don't get stranded.
                    _state.value = LockState.Unlocked
                }
            }
            .launchIn(scope)
    }

    fun markUnlocked() {
        _state.value = LockState.Unlocked
        backgroundedAt = null
    }

    override fun onStart(owner: LifecycleOwner) {
        val prefs = preferences
        if (!prefs.isLockEnabled) {
            _state.value = LockState.Unlocked
            hasEverForegrounded = true
            return
        }
        if (!hasEverForegrounded) {
            hasEverForegrounded = true
            _state.value = LockState.Locked
            return
        }
        val timeout = prefs.autoLockTimeout
        if (timeout == AutoLockTimeout.NEVER) return
        val markedAt = backgroundedAt ?: return
        val elapsed: Duration = markedAt.elapsedNow()
        if (elapsed >= timeout.duration) {
            _state.value = LockState.Locked
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        backgroundedAt = timeSource.markNow()
    }

    /**
     * Called from tests or from settings actions that need to explicitly lock
     * the app (e.g. "Lock now" button).
     */
    fun lockNow() {
        scope.launch { _state.value = LockState.Locked }
    }
}
