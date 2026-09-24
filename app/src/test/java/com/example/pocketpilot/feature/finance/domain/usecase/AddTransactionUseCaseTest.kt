package com.example.pocketpilot.feature.finance.domain.usecase

import com.example.pocketpilot.feature.finance.domain.repository.TransactionRepository
import com.example.pocketpilot.testutil.Fixtures
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddTransactionUseCaseTest {

    private val repository: TransactionRepository = mockk(relaxed = true)
    private val useCase = AddTransactionUseCase(repository)

    @Test
    fun `delegates to repository upsert`() = runTest {
        val transaction = Fixtures.transaction()
        coEvery { repository.upsert(transaction) } returns Unit

        useCase(transaction)

        coVerify(exactly = 1) { repository.upsert(transaction) }
    }
}
