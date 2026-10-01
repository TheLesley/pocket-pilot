package com.example.pocketpilot.feature.auth.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persisted, salted-hash record of a locally-registered account. The email is
 * stored lowercased so lookups are case-insensitive without pulling COLLATE
 * NOCASE into every query.
 */
@Entity(
    tableName = "auth_accounts",
    indices = [Index(value = ["email"], unique = true)]
)
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    @ColumnInfo(name = "display_name")
    val displayName: String?,
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    val salt: String,
    @ColumnInfo(name = "created_at")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "reset_code")
    val resetCode: String? = null,
)
