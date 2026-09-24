package com.example.pocketpilot.feature.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.pocketpilot.core.sync.SyncStatus
import com.example.pocketpilot.feature.finance.data.local.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {

    @Query(
        """
        SELECT * FROM savings_goals
        WHERE sync_status != 'PENDING_DELETE'
        ORDER BY created_at DESC
        """
    )
    fun observeAll(): Flow<List<SavingsGoalEntity>>

    @Query(
        """
        SELECT * FROM savings_goals
        WHERE id = :id AND sync_status != 'PENDING_DELETE'
        """
    )
    fun observeById(id: String): Flow<SavingsGoalEntity?>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun findById(id: String): SavingsGoalEntity?

    @Query("SELECT * FROM savings_goals WHERE sync_status = :status")
    suspend fun findByStatus(status: SyncStatus): List<SavingsGoalEntity>

    @Query("SELECT COUNT(*) FROM savings_goals WHERE sync_status != 'SYNCED'")
    suspend fun pendingCount(): Int

    @Upsert
    suspend fun upsert(entity: SavingsGoalEntity)

    /**
     * Adjusts the saved amount and flips the row into `PENDING_UPDATE` so the
     * sync manager will push the new balance on the next reconciliation pass.
     * A previously-`PENDING_INSERT` row keeps its `PENDING_INSERT` status so
     * it is not upgraded away from the initial create.
     */
    @Query(
        """
        UPDATE savings_goals
        SET saved_minor_units = saved_minor_units + :amountMinorUnits,
            updated_at = :updatedAtEpochMillis,
            local_updated_at = :updatedAtEpochMillis,
            sync_status = CASE
                WHEN sync_status = 'PENDING_INSERT' THEN 'PENDING_INSERT'
                ELSE 'PENDING_UPDATE'
            END
        WHERE id = :id
        """
    )
    suspend fun addContribution(id: String, amountMinorUnits: Long, updatedAtEpochMillis: Long)

    @Query(
        """
        UPDATE savings_goals
        SET sync_status = 'SYNCED',
            last_sync_error = NULL,
            updated_at = :serverUpdatedAtEpochMillis
        WHERE id = :id
        """
    )
    suspend fun markSynced(id: String, serverUpdatedAtEpochMillis: Long)

    @Query(
        """
        UPDATE savings_goals
        SET last_sync_error = :message
        WHERE id = :id
        """
    )
    suspend fun recordSyncError(id: String, message: String?)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteById(id: String)
}
