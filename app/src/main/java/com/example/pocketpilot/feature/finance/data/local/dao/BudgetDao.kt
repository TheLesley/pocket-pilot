package com.example.pocketpilot.feature.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.pocketpilot.core.sync.SyncStatus
import com.example.pocketpilot.feature.finance.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query(
        """
        SELECT * FROM budgets
        WHERE sync_status != 'PENDING_DELETE'
        ORDER BY starts_at DESC
        """
    )
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query(
        """
        SELECT * FROM budgets
        WHERE id = :id AND sync_status != 'PENDING_DELETE'
        """
    )
    fun observeById(id: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun findById(id: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE sync_status = :status")
    suspend fun findByStatus(status: SyncStatus): List<BudgetEntity>

    @Query("SELECT COUNT(*) FROM budgets WHERE sync_status != 'SYNCED'")
    suspend fun pendingCount(): Int

    @Upsert
    suspend fun upsert(entity: BudgetEntity)

    @Query(
        """
        UPDATE budgets
        SET sync_status = 'SYNCED',
            last_sync_error = NULL,
            updated_at = :serverUpdatedAtEpochMillis
        WHERE id = :id
        """
    )
    suspend fun markSynced(id: String, serverUpdatedAtEpochMillis: Long)

    @Query(
        """
        UPDATE budgets
        SET last_sync_error = :message
        WHERE id = :id
        """
    )
    suspend fun recordSyncError(id: String, message: String?)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: String)
}
