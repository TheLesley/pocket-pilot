package com.example.pocketpilot.feature.finance.data.repository

import com.example.pocketpilot.core.sync.SyncStatus
import com.example.pocketpilot.feature.finance.data.local.dao.SavingsGoalDao
import com.example.pocketpilot.feature.finance.data.mapper.toDomain
import com.example.pocketpilot.feature.finance.data.mapper.toEntity
import com.example.pocketpilot.feature.finance.domain.model.SavingsGoal
import com.example.pocketpilot.feature.finance.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavingsGoalRepositoryImpl(private val dao: SavingsGoalDao, private val clock: () -> Long = System::currentTimeMillis) :
    SavingsGoalRepository {

    override fun observeAll(): Flow<List<SavingsGoal>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<SavingsGoal?> = dao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: String): SavingsGoal? = dao.findById(id)?.toDomain()

    override suspend fun upsert(goal: SavingsGoal) {
        val now = clock()
        val existing = dao.findById(goal.id)
        val nextStatus = when (existing?.syncStatus) {
            null -> SyncStatus.PENDING_INSERT
            SyncStatus.PENDING_INSERT -> SyncStatus.PENDING_INSERT
            else -> SyncStatus.PENDING_UPDATE
        }
        dao.upsert(
            goal.toEntity(
                syncStatus = nextStatus,
                localUpdatedAtEpochMillis = now
            )
        )
    }

    override suspend fun addContribution(id: String, amountMinorUnits: Long, updatedAtEpochMillis: Long) {
        dao.addContribution(id, amountMinorUnits, updatedAtEpochMillis)
    }

    override suspend fun delete(id: String) {
        val existing = dao.findById(id) ?: return
        if (existing.syncStatus == SyncStatus.PENDING_INSERT) {
            dao.deleteById(id)
            return
        }
        dao.upsert(
            existing.copy(
                syncStatus = SyncStatus.PENDING_DELETE,
                localUpdatedAtEpochMillis = clock()
            )
        )
    }
}
