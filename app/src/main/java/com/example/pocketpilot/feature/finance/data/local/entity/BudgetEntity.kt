package com.example.pocketpilot.feature.finance.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.pocketpilot.core.sync.SyncStatus
import com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod

@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["sync_status"]),
        Index(value = ["starts_at"]),
        Index(value = ["category_id"])
    ]
)
data class BudgetEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "limit_minor_units")
    val limitMinorUnits: Long,
    @ColumnInfo(name = "currency_code")
    val currencyCode: String,
    val period: BudgetPeriod,
    @ColumnInfo(name = "starts_at")
    val startsAtEpochMillis: Long,
    @ColumnInfo(name = "ends_at")
    val endsAtEpochMillis: Long?,
    @ColumnInfo(name = "category_id")
    val categoryId: String?,
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
