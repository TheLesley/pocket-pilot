package com.example.pocketpilot.feature.finance.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.pocketpilot.core.sync.SyncStatus
import com.example.pocketpilot.feature.finance.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = BudgetEntity::class,
            parentColumns = ["id"],
            childColumns = ["budget_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["budget_id"]),
        Index(value = ["occurred_at"]),
        Index(value = ["category_id"]),
        Index(value = ["sync_status"]),
        Index(value = ["amount_minor_units"])
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    @ColumnInfo(name = "amount_minor_units")
    val amountMinorUnits: Long,
    @ColumnInfo(name = "currency_code")
    val currencyCode: String,
    val type: TransactionType,
    @ColumnInfo(name = "category_id")
    val categoryId: String?,
    @ColumnInfo(name = "budget_id")
    val budgetId: String?,
    @ColumnInfo(name = "occurred_at")
    val occurredAtEpochMillis: Long,
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
