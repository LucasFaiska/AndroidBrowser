package com.lucas.oliveira.android.browser.core.webview

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import com.lucas.oliveira.android.browser.R
import org.junit.Assert

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BrowserViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `error page should be displayed when state has error`() {
        val state = BrowserState()
        state.error = BrowserError(
            errorCode = -1,
            description = "No connection",
            failingUrl = "https://example.com"
        )

        composeTestRule.setContent {
            BrowserView(state = state)
        }

        val context = RuntimeEnvironment.getApplication()
        composeTestRule.onNodeWithText(context.getString(R.string.error_oops_something_went_wrong)).assertIsDisplayed()
        composeTestRule.onNodeWithText("No connection").assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.error_url_label, "https://example.com")).assertIsDisplayed()
    }

    @Test
    fun `retry button should trigger reload and clear error`() {
        val state = BrowserState()
        state.error = BrowserError(
            errorCode = -1,
            description = "No connection",
            failingUrl = "https://example.com"
        )

        composeTestRule.setContent {
            BrowserView(state = state)
        }

        val context = RuntimeEnvironment.getApplication()
        composeTestRule.onNodeWithText(context.getString(R.string.error_retry)).performClick()

        assertNull(state.error)
        assertEquals(BrowserAction.Reload, state.pendingAction)
    }

    private fun assertNull(actual: Any?) {
        Assert.assertNull(actual)
    }
}
