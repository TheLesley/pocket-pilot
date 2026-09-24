package com.example.pocketpilot.feature.auth.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class User(val id: String, val email: String, val displayName: String?)
