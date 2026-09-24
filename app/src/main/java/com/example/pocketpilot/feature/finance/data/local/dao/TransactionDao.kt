package com.example.pocketpilot.feature.finance.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.pocketpilot.core.sync.SyncStatus
import com.example.pocketpilot.feature.finance.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    /**
     * Runtime-composed search / filter query. Callers must build the
     * `SupportSQLiteQuery` via
     * [com.example.pocketpilot.feature.finance.data.local.query.TransactionQuerySqlBuilder]
     * so the `observedEntities` list stays aligned with the tables the query
     * touches (invalidations only fire for declared tables).
     */
    @RawQuery(observedEntities = [TransactionEntity::class])
    fun observeByRawQuery(query: SupportSQLiteQuery): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE sync_status != 'PENDING_DELETE'
        ORDER BY occurred_at DESC
        """
    )
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE budget_id = :budgetId AND sync_status != 'PENDING_DELETE'
        ORDER BY occurred_at DESC
        """
    )
    fun observeByBudget(budgetId: String): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE occurred_at BETWEEN :fromEpochMillis AND :toEpochMillis
          AND sync_status != 'PENDING_DELETE'
        ORDER BY occurred_at DESC
        """
    )
    fun observeInRange(fromEpochMillis: Long, toEpochMillis: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE id = :id AND sync_status != 'PENDING_DELETE'
        """
    )
    fun observeById(id: String): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun findById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE sync_status = :status")
    suspend fun findByStatus(status: SyncStatus): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE sync_status != 'SYNCED'")
    suspend fun pendingCount(): Int

    @Upsert
    suspend fun upsert(entity: TransactionEntity)

    /**
     * Marks a row as reconciled with the server. Timestamps come from the
     * remote canonical record.
     */
    @Query(
        """
        UPDATE transactions
        SET sync_status = 'SYNCED',
            last_sync_error = NULL,
            updated_at = :serverUpdatedAtEpochMillis
        WHERE id = :id
        """
    )
    suspend fun markSynced(id: String, serverUpdatedAtEpochMillis: Long)

    @Query(
        """
        UPDATE transactions
        SET last_sync_error = :message
        WHERE id = :id
        """
    )
    suspend fun recordSyncError(id: String, message: String?)

    /** Actual removal from disk – used after a remote DELETE succeeds. */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)
}
