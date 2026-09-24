package com.example.pocketpilot.feature.finance.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.example.pocketpilot.core.sync.SyncStatus
import com.example.pocketpilot.feature.finance.data.local.PocketPilotDatabase
import com.example.pocketpilot.feature.finance.data.local.entity.BudgetEntity
import com.example.pocketpilot.feature.finance.domain.model.BudgetPeriod
import com.example.pocketpilot.testutil.TestApplication
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, sdk = [34])
class BudgetDaoTest {

    private lateinit var database: PocketPilotDatabase
    private lateinit var dao: BudgetDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PocketPilotDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        dao = database.budgetDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun entity(id: String, startsAt: Long = 0L, syncStatus: SyncStatus = SyncStatus.SYNCED): BudgetEntity = BudgetEntity(
        id = id,
        name = "B-$id",
        limitMinorUnits = 10_000L,
        currencyCode = "USD",
        period = BudgetPeriod.MONTHLY,
        startsAtEpochMillis = startsAt,
        endsAtEpochMillis = null,
        categoryId = null,
        createdAtEpochMillis = startsAt,
        updatedAtEpochMillis = startsAt,
        syncStatus = syncStatus,
        localUpdatedAtEpochMillis = startsAt,
        lastSyncError = null
    )

    @Test
    fun `observeAll excludes PENDING_DELETE and sorts by starts_at desc`() = runTest {
        dao.upsert(entity(id = "old", startsAt = 100L))
        dao.upsert(entity(id = "new", startsAt = 500L))
        dao.upsert(entity(id = "tombstone", startsAt = 999L, syncStatus = SyncStatus.PENDING_DELETE))

        dao.observeAll().test {
            val rows = awaitItem()
            assertEquals(listOf("new", "old"), rows.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `findByStatus returns rows in the requested state`() = runTest {
        dao.upsert(entity(id = "1", syncStatus = SyncStatus.PENDING_INSERT))
        dao.upsert(entity(id = "2", syncStatus = SyncStatus.SYNCED))

        val pending = dao.findByStatus(SyncStatus.PENDING_INSERT)
        assertEquals(listOf("1"), pending.map { it.id })
    }

    @Test
    fun `recordSyncError writes the message onto the row`() = runTest {
        dao.upsert(entity(id = "1"))

        dao.recordSyncError("1", "timeout")

        val row = dao.findById("1")
        assertNotNull(row)
        assertEquals("timeout", row!!.lastSyncError)
    }
}
