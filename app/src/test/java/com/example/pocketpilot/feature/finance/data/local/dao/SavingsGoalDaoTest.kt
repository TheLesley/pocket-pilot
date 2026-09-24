package com.example.pocketpilot.feature.finance.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.pocketpilot.core.sync.SyncStatus
import com.example.pocketpilot.feature.finance.data.local.PocketPilotDatabase
import com.example.pocketpilot.feature.finance.data.local.entity.SavingsGoalEntity
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
class SavingsGoalDaoTest {

    private lateinit var database: PocketPilotDatabase
    private lateinit var dao: SavingsGoalDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PocketPilotDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
        dao = database.savingsGoalDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun goal(id: String, saved: Long = 0L, syncStatus: SyncStatus = SyncStatus.SYNCED): SavingsGoalEntity = SavingsGoalEntity(
        id = id,
        name = "G-$id",
        targetMinorUnits = 100_000L,
        savedMinorUnits = saved,
        currencyCode = "USD",
        targetDateEpochMillis = null,
        note = null,
        createdAtEpochMillis = 0L,
        updatedAtEpochMillis = 0L,
        syncStatus = syncStatus,
        localUpdatedAtEpochMillis = 0L,
        lastSyncError = null
    )

    @Test
    fun `addContribution increases saved amount and marks PENDING_UPDATE for synced rows`() = runTest {
        dao.upsert(goal(id = "g1", saved = 100L, syncStatus = SyncStatus.SYNCED))

        dao.addContribution("g1", amountMinorUnits = 250L, updatedAtEpochMillis = 5_000L)

        val row = dao.findById("g1")
        assertNotNull(row)
        assertEquals(350L, row!!.savedMinorUnits)
        assertEquals(SyncStatus.PENDING_UPDATE, row.syncStatus)
        assertEquals(5_000L, row.updatedAtEpochMillis)
    }

    @Test
    fun `addContribution preserves PENDING_INSERT state`() = runTest {
        dao.upsert(goal(id = "g2", saved = 50L, syncStatus = SyncStatus.PENDING_INSERT))

        dao.addContribution("g2", amountMinorUnits = 25L, updatedAtEpochMillis = 1L)

        assertEquals(SyncStatus.PENDING_INSERT, dao.findById("g2")!!.syncStatus)
    }

    @Test
    fun `markSynced flips row to SYNCED and clears sync error`() = runTest {
        dao.upsert(goal(id = "g1", syncStatus = SyncStatus.PENDING_UPDATE))
        dao.recordSyncError("g1", "server 500")

        dao.markSynced("g1", serverUpdatedAtEpochMillis = 7_777L)

        val row = dao.findById("g1")!!
        assertEquals(SyncStatus.SYNCED, row.syncStatus)
        assertEquals(null, row.lastSyncError)
        assertEquals(7_777L, row.updatedAtEpochMillis)
    }
}
