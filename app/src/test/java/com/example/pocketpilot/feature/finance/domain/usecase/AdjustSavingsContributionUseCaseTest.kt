package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.SavingsGoal
import com.example.pocketpilot.feature.finance.domain.repository.SavingsGoalRepository
import com.example.pocketpilot.testutil.Fixtures
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdjustSavingsContributionUseCaseTest {

    private val repository: SavingsGoalRepository = mockk(relaxed = true)
    private val useCase = AdjustSavingsContributionUseCase(repository, now = { 42L })

    @Test
    fun `zero delta short-circuits with NoChange`() = runTest {
        val result = useCase("goal-1", 0L)
        assertEquals(AdjustSavingsContributionUseCase.Result.NoChange, result)
    }

    @Test
    fun `missing goal returns NotFound`() = runTest {
        coEvery { repository.getById("missing") } returns null

        val result = useCase("missing", 500L)

        assertEquals(AdjustSavingsContributionUseCase.Result.NotFound, result)
    }

    @Test
    fun `positive delta increases saved balance and stamps updated time`() = runTest {
        val goal = Fixtures.savingsGoal(id = "g1", savedMinorUnits = 100L)
        coEvery { repository.getById("g1") } returns goal

        val captured: CapturingSlot<SavingsGoal> = slot()
        coEvery { repository.upsert(capture(captured)) } returns Unit

        val result = useCase("g1", 250L)

        assertEquals(
            AdjustSavingsContributionUseCase.Result.Applied(newSavedMinorUnits = 350L),
            result
        )
        assertEquals(350L, captured.captured.savedMinorUnits)
        assertEquals(42L, captured.captured.updatedAtEpochMillis)
        coVerify(exactly = 1) { repository.upsert(any()) }
    }

    @Test
    fun `withdrawal larger than balance clamps to zero`() = runTest {
        val goal = Fixtures.savingsGoal(id = "g1", savedMinorUnits = 100L)
        coEvery { repository.getById("g1") } returns goal

        val captured: CapturingSlot<SavingsGoal> = slot()
        coEvery { repository.upsert(capture(captured)) } returns Unit

        val result = useCase("g1", -500L)

        assertTrue(result is AdjustSavingsContributionUseCase.Result.Applied)
        assertEquals(0L, captured.captured.savedMinorUnits)
    }
}
