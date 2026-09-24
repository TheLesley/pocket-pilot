package com.example.pocketpilot.feature.finance.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.pocketpilot.core.sync.SyncStatus
import com.example.pocketpilot.feature.finance.data.local.PocketPilotDatabase
import com.example.pocketpilot.feature.finance.data.local.entity.TransactionEntity
import com.example.pocketpilot.feature.finance.domain.model.TransactionType
import com.example.pocketpilot.testutil.TestApplication
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, sdk = [34])
class TransactionDaoTest {

    private lateinit var database: PocketPilotDatabase
    private lateinit var dao: TransactionDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PocketPilotDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        dao = database.transactionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun entity(
        id: String,
        amount: Long = 500L,
        type: TransactionType = TransactionType.EXPENSE,
        occurredAt: Long = 1_000L,
        syncStatus: SyncStatus = SyncStatus.SYNCED,
        budgetId: String? = null
    ): TransactionEntity = TransactionEntity(
        id = id,
        title = "T-$id",
        amountMinorUnits = amount,
        currencyCode = "USD",
        type = type,
        categoryId = null,
        budgetId = budgetId,
        occurredAtEpochMillis = occurredAt,
        note = null,
        createdAtEpochMillis = occurredAt,
        updatedAtEpochMillis = occurredAt,
        syncStatus = syncStatus,
        localUpdatedAtEpochMillis = occurredAt,
        lastSyncError = null
    )

    @Test
    fun `upsert then findById returns the inserted row`() = runTest {
        val entity = entity(id = "a")

        dao.upsert(entity)

        assertEquals(entity, dao.findById("a"))
    }

    @Test
    fun `observeAll excludes rows marked PENDING_DELETE and orders by occurred_at desc`() = runTest {
        dao.upsert(entity(id = "older", occurredAt = 100L))
        dao.upsert(entity(id = "newer", occurredAt = 200L))
        dao.upsert(entity(id = "gone", occurredAt = 300L, syncStatus = SyncStatus.PENDING_DELETE))

        dao.observeAll().test {
            val rows = awaitItem()
            assertEquals(listOf("newer", "older"), rows.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeInRange returns only rows inside window`() = runTest {
        dao.upsert(entity(id = "before", occurredAt = 50L))
        dao.upsert(entity(id = "inside", occurredAt = 150L))
        dao.upsert(entity(id = "after", occurredAt = 500L))

        dao.observeInRange(fromEpochMillis = 100L, toEpochMillis = 200L).test {
            val rows = awaitItem()
            assertEquals(listOf("inside"), rows.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `markSynced flips status and updates updated_at`() = runTest {
        dao.upsert(entity(id = "x", syncStatus = SyncStatus.PENDING_UPDATE))

        dao.markSynced("x", serverUpdatedAtEpochMillis = 9_999L)

        val row = dao.findById("x")
        assertNotNull(row)
        assertEquals(SyncStatus.SYNCED, row!!.syncStatus)
        assertEquals(9_999L, row.updatedAtEpochMillis)
    }

    @Test
    fun `deleteById physically removes the row`() = runTest {
        dao.upsert(entity(id = "x"))

        dao.deleteById("x")

        assertNull(dao.findById("x"))
    }

    @Test
    fun `pendingCount counts everything not SYNCED`() = runTest {
        dao.upsert(entity(id = "1", syncStatus = SyncStatus.SYNCED))
        dao.upsert(entity(id = "2", syncStatus = SyncStatus.PENDING_INSERT))
        dao.upsert(entity(id = "3", syncStatus = SyncStatus.PENDING_UPDATE))
        dao.upsert(entity(id = "4", syncStatus = SyncStatus.PENDING_DELETE))

        assertEquals(3, dao.pendingCount())
    }
}
