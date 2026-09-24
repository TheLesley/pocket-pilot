package com.example.pocketpilot.feature.security.domain.model

/**
 * The reasons the device may not be able to satisfy a strong biometric prompt.
 * Mirrors the states surfaced by AndroidX BiometricManager so the UI can render
 * an accurate message without importing the framework enum.
 */
enum class BiometricAvailability {
    /** Biometrics are enrolled and can be used right now. */
    Available,

    /** The hardware exists but no biometrics are enrolled. */
    NotEnrolled,

    /** No compatible biometric hardware on this device. */
    NoHardware,

    /** Hardware exists but is temporarily unavailable (e.g. warming up). */
    HardwareUnavailable,

    /** OS-level security update required before biometrics can be used. */
    SecurityUpdateRequired,

    /** Unknown or unsupported status. */
    Unknown
}
