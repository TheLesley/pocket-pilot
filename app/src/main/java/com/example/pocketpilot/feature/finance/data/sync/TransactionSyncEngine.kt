package com.example.pocketpilot.feature.finance.data.sync

import com.example.pocketpilot.core.network.NetworkException
import com.example.pocketpilot.core.sync.ConflictStrategy
import com.example.pocketpilot.core.sync.EntitySync
import com.example.pocketpilot.core.sync.EntitySyncResult
import com.example.pocketpilot.core.sync.SyncConfig
import com.example.pocketpilot.core.sync.SyncStatus
import com.example.pocketpilot.core.sync.retrying
import com.example.pocketpilot.feature.finance.data.local.dao.TransactionDao
import com.example.pocketpilot.feature.finance.data.local.entity.TransactionEntity
import com.example.pocketpilot.feature.finance.data.mapper.toDomain
import com.example.pocketpilot.feature.finance.data.mapper.toEntity
import com.example.pocketpilot.feature.finance.data.remote.TransactionRemoteDataSource
import com.example.pocketpilot.feature.finance.domain.model.Transaction
import kotlinx.coroutines.CancellationException

/**
 * Pushes locally-pending [TransactionEntity] rows to the remote API and
 * reconciles the local copy with whatever the server returns. See
 * [com.example.pocketpilot.core.sync.SyncManager] for how the finance engines
 * are wired together.
 */
class TransactionSyncEngine(private val dao: TransactionDao, private val remote: TransactionRemoteDataSource) : EntitySync {

    override suspend fun pendingCount(): Int = dao.pendingCount()

    override suspend fun sync(config: SyncConfig): EntitySyncResult {
        val inserts = dao.findByStatus(SyncStatus.PENDING_INSERT)
        val updates = dao.findByStatus(SyncStatus.PENDING_UPDATE)
        val deletes = dao.findByStatus(SyncStatus.PENDING_DELETE)

        var pushed = 0
        var failed = 0
        val errors = mutableListOf<Throwable>()

        for (row in inserts) {
            runCatching { pushInsert(row, config) }
                .onSuccess { pushed += 1 }
                .onFailure { handleFailure(row, it, errors) { failed += 1 } }
        }
        for (row in updates) {
            runCatching { pushUpdate(row, config) }
                .onSuccess { pushed += 1 }
                .onFailure { handleFailure(row, it, errors) { failed += 1 } }
        }
        for (row in deletes) {
            runCatching { pushDelete(row, config) }
                .onSuccess { pushed += 1 }
                .onFailure { handleFailure(row, it, errors) { failed += 1 } }
        }
        return EntitySyncResult(pushed = pushed, failed = failed, errors = errors)
    }

    private suspend fun pushInsert(row: TransactionEntity, config: SyncConfig) {
        try {
            val remoteRow = retrying(config) { remote.create(row.toDomain()) }
            overwriteLocalWithServer(remoteRow)
        } catch (ce: CancellationException) {
            throw ce
        } catch (client: NetworkException.ClientError) {
            // Likely 409 – server already has a record with this id. Adopt it.
            resolveConflict(row, config)
        }
    }

    private suspend fun pushUpdate(row: TransactionEntity, config: SyncConfig) {
        try {
            val remoteRow = retrying(config) { remote.update(row.toDomain()) }
            reconcile(local = row, server = remoteRow, config = config)
        } catch (ce: CancellationException) {
            throw ce
        } catch (client: NetworkException.ClientError) {
            resolveConflict(row, config)
        }
    }

    private suspend fun pushDelete(row: TransactionEntity, config: SyncConfig) {
        try {
            retrying(config) { remote.delete(row.id) }
        } catch (ce: CancellationException) {
            throw ce
        } catch (client: NetworkException.ClientError) {
            // 404 → already gone on the server; fall through to hard-delete.
        }
        dao.deleteById(row.id)
    }

    private suspend fun resolveConflict(local: TransactionEntity, config: SyncConfig) {
        val server = retrying(config) { remote.get(local.id) }
        reconcile(local = local, server = server, config = config)
    }

    private suspend fun reconcile(local: TransactionEntity, server: Transaction, config: SyncConfig) {
        when (config.conflictStrategy) {
            ConflictStrategy.SERVER_WINS -> overwriteLocalWithServer(server)
            ConflictStrategy.CLIENT_TIMESTAMP -> {
                if (local.localUpdatedAtEpochMillis > server.updatedAtEpochMillis) {
                    val forced = retrying(config) { remote.update(local.toDomain()) }
                    overwriteLocalWithServer(forced)
                } else {
                    overwriteLocalWithServer(server)
                }
            }
        }
    }

    private suspend fun overwriteLocalWithServer(server: Transaction) {
        dao.upsert(server.toEntity(syncStatus = SyncStatus.SYNCED))
    }

    private suspend fun handleFailure(
        row: TransactionEntity,
        throwable: Throwable,
        errors: MutableList<Throwable>,
        markFailed: () -> Unit
    ) {
        if (throwable is CancellationException) throw throwable
        markFailed()
        errors += throwable
        dao.recordSyncError(row.id, throwable.message)
    }
}
