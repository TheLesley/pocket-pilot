package com.example.pocketpilot.feature.finance.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.pocketpilot.core.sync.SyncStatus

@Entity(
    tableName = "savings_goals",
    indices = [
        Index(value = ["sync_status"]),
        Index(value = ["created_at"])
    ]
)
data class SavingsGoalEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "target_minor_units")
    val targetMinorUnits: Long,
    @ColumnInfo(name = "saved_minor_units")
    val savedMinorUnits: Long,
    @ColumnInfo(name = "currency_code")
    val currencyCode: String,
    @ColumnInfo(name = "target_date")
    val targetDateEpochMillis: Long?,
    val note: String?,
    @ColumnInfo(name = "created_at")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAtEpochMillis: Long,
    @ColumnInfo(name = "sync_status", defaultValue = "SYNCED")
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    @ColumnInfo(name = "local_updated_at", defaultValue = "0")
    val localUpdatedAtEpochMillis: Long = 0L,
    @ColumnInfo(name = "last_sync_error")
    val lastSyncError: String? = null
)
