package com.example.pocketpilot.feature.auth.presentation.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.testutil.TestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, sdk = [34])
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `login button is disabled when email and password are blank`() {
        composeRule.setContent {
            PocketPilotTheme {
                LoginScreen(state = LoginState(), onEvent = {})
            }
        }

        composeRule.onNodeWithText("Log in").assertIsNotEnabled()
    }

    @Test
    fun `login button is enabled and submits when both fields are filled`() {
        val events = mutableListOf<LoginEvent>()
        val state = LoginState(email = "user@example.com", password = "secret12")
        composeRule.setContent {
            PocketPilotTheme {
                LoginScreen(state = state, onEvent = events::add)
            }
        }

        composeRule.onNodeWithText("Log in")
            .assertIsEnabled()
            .performClick()

        assertTrue(LoginEvent.Submit in events)
    }

    @Test
    fun `submit error message is shown to the user`() {
        val state = LoginState(
            email = "user@example.com",
            password = "wrong",
            submitError = "Email or password is incorrect"
        )
        composeRule.setContent {
            PocketPilotTheme {
                LoginScreen(state = state, onEvent = {})
            }
        }

        composeRule
            .onNodeWithText("Email or password is incorrect")
            .assertIsDisplayed()
    }

    @Test
    fun `Forgot password link fires a ForgotPasswordClicked event`() {
        val events = mutableListOf<LoginEvent>()
        composeRule.setContent {
            PocketPilotTheme {
                LoginScreen(state = LoginState(), onEvent = events::add)
            }
        }

        composeRule.onNodeWithText("Forgot password?").performClick()

        assertEquals(LoginEvent.ForgotPasswordClicked, events.first())
    }

    @Test
    fun `typing in email field emits EmailChanged events`() {
        val events = mutableListOf<LoginEvent>()
        composeRule.setContent {
            PocketPilotTheme {
                LoginScreen(state = LoginState(), onEvent = events::add)
            }
        }

        composeRule.onNodeWithText("Email").performTextInput("hi")

        assertTrue(events.any { it is LoginEvent.EmailChanged })
    }
}
