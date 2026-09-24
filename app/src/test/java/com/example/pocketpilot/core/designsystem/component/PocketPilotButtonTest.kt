package com.example.pocketpilot.core.designsystem.component

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme
import com.example.pocketpilot.testutil.TestApplication
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, sdk = [34])
class PocketPilotButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `primary button fires onClick when tapped`() {
        var clicked = false
        composeRule.setContent {
            PocketPilotTheme {
                PocketPilotPrimaryButton(text = "Save", onClick = { clicked = true })
            }
        }

        composeRule.onNodeWithText("Save")
            .assertIsEnabled()
            .assertHasClickAction()
            .performClick()

        assertTrue(clicked)
    }

    @Test
    fun `primary button is disabled while loading`() {
        composeRule.setContent {
            PocketPilotTheme {
                PocketPilotPrimaryButton(
                    text = "Save",
                    onClick = {},
                    loading = true
                )
            }
        }

        composeRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun `secondary button reflects enabled flag`() {
        composeRule.setContent {
            PocketPilotTheme {
                PocketPilotSecondaryButton(
                    text = "Cancel",
                    onClick = {},
                    enabled = false
                )
            }
        }

        composeRule.onNodeWithText("Cancel").assertIsNotEnabled()
    }
}
