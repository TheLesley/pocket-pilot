package com.example.pocketpilot.feature.finance.data.local.converter

import androidx.room.TypeConverter
import com.example.pocketpilot.core.sync.SyncStatus
import com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod
import com.example.pocketpilot.feature.finance.domain.model.TransactionType

class FinanceTypeConverters {

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromBudgetPeriod(value: BudgetPeriod): String = value.name

    @TypeConverter
    fun toBudgetPeriod(value: String): BudgetPeriod = BudgetPeriod.valueOf(value)

    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}
