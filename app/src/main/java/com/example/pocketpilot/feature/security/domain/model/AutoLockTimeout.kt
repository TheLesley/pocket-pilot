package com.example.pocketpilot.feature.security.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * How long the app is allowed to stay unlocked after moving to the background
 * before the [com.example.pocketpilot.feature.security.presentation.LockScreen]
 * is re-shown. Modelled as a closed set so the settings UI can present a
 * radio-style picker without hand-rolled parsing.
 */
enum class AutoLockTimeout(val duration: Duration) {
    IMMEDIATELY(ZERO),
    THIRTY_SECONDS(30.seconds),
    ONE_MINUTE(1.minutes),
    FIVE_MINUTES(5.minutes),
    FIFTEEN_MINUTES(15.minutes),
    NEVER(Duration.INFINITE);

    companion object {
        val Default: AutoLockTimeout = IMMEDIATELY

        fun fromNameOrDefault(name: String?): AutoLockTimeout = name
            ?.let { runCatching { valueOf(it) }.getOrNull() }
            ?: Default
    }
}
