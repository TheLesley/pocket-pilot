package com.example.pocketpilot.feature.finance.presentation.budget.form

import app.cash.turbine.test
import com.example.pocketpilot.feature.finance.domain.model.Budget
import com.example.pocketpilot.feature.finance.domain.usecase.AddBudgetUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.EditBudgetUseCase
import com.example.pocketpilot.feature.finance.domain.usecase.GetBudgetUseCase
import com.example.pocketpilot.testutil.Fixtures
import com.example.pocketpilot.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class BudgetFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val addBudget: AddBudgetUseCase = mockk()
    private val editBudget: EditBudgetUseCase = mockk()
    private val getBudget: GetBudgetUseCase = mockk()

    private fun addViewModel() = BudgetFormViewModel(
        initialMode = BudgetFormMode.Add,
        addBudgetUseCase = addBudget,
        editBudgetUseCase = editBudget,
        getBudgetUseCase = getBudget
    )

    @Test
    fun `Submit with empty form surfaces per-field errors and does not persist`() = runTest {
        val vm = addViewModel()

        vm.onEvent(BudgetFormEvent.Submit)

        val s = vm.state.value
        assertNotNull(s.nameError)
        assertNotNull(s.limitError)
        assertFalse(s.isSubmitting)
        coVerify(exactly = 0) { addBudget(any()) }
    }

    @Test
    fun `end date before start date surfaces dateError`() = runTest {
        val vm = addViewModel()
        vm.onEvent(BudgetFormEvent.NameChanged("Groceries"))
        vm.onEvent(BudgetFormEvent.LimitChanged("100"))
        vm.onEvent(BudgetFormEvent.StartDateChanged(2_000L))
        vm.onEvent(BudgetFormEvent.EndDateChanged(1_000L))

        vm.onEvent(BudgetFormEvent.Submit)

        assertNotNull(vm.state.value.dateError)
        coVerify(exactly = 0) { addBudget(any()) }
    }

    @Test
    fun `successful add persists a budget and emits Saved effect`() = runTest {
        val slot = slot<Budget>()
        coEvery { addBudget(capture(slot)) } returns Unit

        val vm = addViewModel()
        vm.effect.test {
            vm.onEvent(BudgetFormEvent.NameChanged("Coffee"))
            vm.onEvent(BudgetFormEvent.LimitChanged("125.50"))
            vm.onEvent(BudgetFormEvent.CategoryChanged("coffee"))
            vm.onEvent(BudgetFormEvent.Submit)

            assertEquals(BudgetFormEffect.Saved, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals("Coffee", slot.captured.name)
        assertEquals(12_550L, slot.captured.limitMinorUnits)
        assertEquals("coffee", slot.captured.categoryId)
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test
    fun `edit mode loads existing budget into the form`() = runTest {
        val existing = Fixtures.budget(
            id = "b1",
            name = "Rent",
            limitMinorUnits = 250_000L,
            currencyCode = "USD",
            categoryId = "housing"
        )
        coEvery { getBudget("b1") } returns existing

        val vm = BudgetFormViewModel(
            initialMode = BudgetFormMode.Edit("b1"),
            addBudgetUseCase = addBudget,
            editBudgetUseCase = editBudget,
            getBudgetUseCase = getBudget
        )

        val s = vm.state.value
        assertEquals("Rent", s.name)
        assertEquals("housing", s.category)
        assertNull(s.loadError)
    }
}
