package com.example.pocketpilot.feature.dashboard.presentation

import app.cash.turbine.test
import com.example.pocketpilot.core.ui.UiState
import com.example.pocketpilot.feature.dashboard.domain.model.DashboardSummary
import com.example.pocketpilot.feature.dashboard.domain.model.MonthRange
import com.example.pocketpilot.feature.dashboard.domain.usecase.ObserveDashboardSummaryUseCase
import com.example.pocketpilot.testutil.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val observeUseCase: ObserveDashboardSummaryUseCase = mockk()

    private fun sampleSummary(): DashboardSummary = DashboardSummary(
        currencyCode = "USD",
        currentBalanceMinorUnits = 10_000L,
        monthlyIncomeMinorUnits = 5_000L,
        monthlyExpenseMinorUnits = 2_000L,
        monthlyBudgetMinorUnits = 8_000L,
        categoryBreakdown = emptyList(),
        recentTransactions = emptyList(),
        monthRange = MonthRange(0L, 1L)
    )

    @Test
    fun `emits Loading then Success when the summary stream produces data`() = runTest {
        val emissions = MutableSharedFlow<DashboardSummary>(replay = 0, extraBufferCapacity = 1)
        every { observeUseCase() } returns emissions

        val viewModel = DashboardViewModel(observeUseCase)

        viewModel.state.test {
            assertTrue(awaitItem().summary is UiState.Loading)

            emissions.emit(sampleSummary())
            val next = awaitItem()
            assertTrue(next.summary is UiState.Success)
            assertEquals(10_000L, (next.summary as UiState.Success).data.currentBalanceMinorUnits)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Error when the summary stream throws`() = runTest {
        every { observeUseCase() } returns flow { throw IllegalStateException("boom") }

        val viewModel = DashboardViewModel(observeUseCase)

        viewModel.state.test {
            // Drop initial Loading; UnconfinedTestDispatcher may collapse to final state.
            var latest = awaitItem().summary
            while (latest is UiState.Loading) latest = awaitItem().summary
            assertTrue(latest is UiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ViewAllTransactionsClicked emits NavigateToTransactionList`() = runTest {
        val emissions = MutableSharedFlow<DashboardSummary>(replay = 0, extraBufferCapacity = 1)
        every { observeUseCase() } returns emissions

        val viewModel = DashboardViewModel(observeUseCase)

        viewModel.effect.test {
            viewModel.onEvent(DashboardEvent.ViewAllTransactionsClicked)
            assertEquals(DashboardEffect.NavigateToTransactionList, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
