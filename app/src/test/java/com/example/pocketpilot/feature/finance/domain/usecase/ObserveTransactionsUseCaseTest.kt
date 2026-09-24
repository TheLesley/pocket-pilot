package com.example.pocketpilot.feature.finance.domain.usecase

import app.cash.turbine.test
import com.example.pocketpilot.feature.finance.domain.model.TransactionQuery
import com.example.pocketpilot.feature.finance.domain.repository.TransactionRepository
import com.example.pocketpilot.testutil.Fixtures
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveTransactionsUseCaseTest {

    @Test
    fun `emits repository stream as-is`() = runTest {
        val repository: TransactionRepository = mockk()
        val emissions = MutableSharedFlow<List<com.example.pocketpilot.feature.finance.domain.model.Transaction>>(replay = 1)
        every { repository.observe(any<TransactionQuery>()) } returns emissions

        val useCase = ObserveTransactionsUseCase(repository)

        useCase().test {
            emissions.emit(listOf(Fixtures.transaction()))
            assertEquals(1, awaitItem().size)

            emissions.emit(emptyList())
            assertEquals(0, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
