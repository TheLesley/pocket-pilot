package com.example.pocketpilot.feature.auth.presentation.login

import app.cash.turbine.test
import com.example.pocketpilot.feature.auth.domain.model.AuthException
import com.example.pocketpilot.feature.auth.domain.model.User
import com.example.pocketpilot.feature.auth.domain.usecase.LoginUseCase
import com.example.pocketpilot.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val loginUseCase: LoginUseCase = mockk()
    private lateinit var viewModel: LoginViewModel

    @Test
    fun `initial state is empty and cannot submit`() = runTest {
        viewModel = LoginViewModel(loginUseCase)

        viewModel.state.test {
            val initial = awaitItem()
            assertEquals("", initial.email)
            assertEquals("", initial.password)
            assertFalse(initial.canSubmit)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `email and password changes clear previous errors`() = runTest {
        viewModel = LoginViewModel(loginUseCase)

        viewModel.onEvent(LoginEvent.EmailChanged("bad"))
        viewModel.onEvent(LoginEvent.PasswordChanged(""))
        viewModel.onEvent(LoginEvent.Submit) // triggers validation errors

        // now clear them
        viewModel.onEvent(LoginEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("secret12"))

        val s = viewModel.state.value
        assertNull(s.emailError)
        assertNull(s.passwordError)
        assertTrue(s.canSubmit)
    }

    @Test
    fun `submit with invalid email surfaces validation error without calling use case`() = runTest {
        viewModel = LoginViewModel(loginUseCase)
        viewModel.onEvent(LoginEvent.EmailChanged("not-an-email"))
        viewModel.onEvent(LoginEvent.PasswordChanged("secret12"))

        viewModel.onEvent(LoginEvent.Submit)

        val s = viewModel.state.value
        assertNotNull(s.emailError)
        assertFalse(s.isSubmitting)
        coVerify(exactly = 0) { loginUseCase(any(), any()) }
    }

    @Test
    fun `successful submit emits NavigateToHome effect and clears submitting`() = runTest {
        coEvery { loginUseCase("user@example.com", "secret12") } returns User(
            id = "u1",
            email = "user@example.com",
            displayName = null
        )
        viewModel = LoginViewModel(loginUseCase)

        viewModel.effect.test {
            viewModel.onEvent(LoginEvent.EmailChanged("user@example.com"))
            viewModel.onEvent(LoginEvent.PasswordChanged("secret12"))
            viewModel.onEvent(LoginEvent.Submit)

            assertEquals(LoginEffect.NavigateToHome, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertFalse(viewModel.state.value.isSubmitting)
    }

    @Test
    fun `failed submit surfaces a user-facing error message`() = runTest {
        coEvery { loginUseCase(any(), any()) } throws AuthException.InvalidCredentials
        viewModel = LoginViewModel(loginUseCase)

        viewModel.onEvent(LoginEvent.EmailChanged("user@example.com"))
        viewModel.onEvent(LoginEvent.PasswordChanged("secret12"))
        viewModel.onEvent(LoginEvent.Submit)

        val s = viewModel.state.value
        assertFalse(s.isSubmitting)
        assertEquals("Email or password is incorrect", s.submitError)
    }

    @Test
    fun `ForgotPasswordClicked emits NavigateToForgotPassword`() = runTest {
        viewModel = LoginViewModel(loginUseCase)
        viewModel.effect.test {
            viewModel.onEvent(LoginEvent.ForgotPasswordClicked)
            assertEquals(LoginEffect.NavigateToForgotPassword, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
