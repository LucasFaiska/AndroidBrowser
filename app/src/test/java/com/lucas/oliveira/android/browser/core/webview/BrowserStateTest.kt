package com.lucas.oliveira.android.browser.core.webview

import androidx.compose.runtime.saveable.SaverScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BrowserStateTest {

    private val testSaverScope = SaverScope { true }

    @Test
    fun `initial state should match provided values`() {
        val state = BrowserState(initialUrl = "https://example.com", initialTitle = "Example")
        assertEquals("https://example.com", state.url)
        assertEquals("Example", state.title)
        assertFalse(state.isLoading)
        assertFalse(state.canGoBack)
        assertFalse(state.canGoForward)
        assertNull(state.error)
        assertEquals(BrowserAction.LoadUrl("https://example.com"), state.pendingAction)
    }

    @Test
    fun `default initial state without parameters`() {
        val state = BrowserState()
        assertNull(state.url)
        assertNull(state.title)
        assertFalse(state.isLoading)
        assertFalse(state.canGoBack)
        assertFalse(state.canGoForward)
        assertNull(state.error)
        assertNull(state.pendingAction)
    }

    @Test
    fun `loadUrl should update pendingAction and clear error`() {
        val state = BrowserState()
        state.error = BrowserError(-1, "Error", "url")

        state.loadUrl("https://google.com")

        assertEquals(BrowserAction.LoadUrl("https://google.com"), state.pendingAction)
        assertNull(state.error)
    }

    @Test
    fun `goBack should update pendingAction`() {
        val state = BrowserState()
        state.goBack()
        assertEquals(BrowserAction.GoBack, state.pendingAction)
    }

    @Test
    fun `goForward should update pendingAction`() {
        val state = BrowserState()
        state.goForward()
        assertEquals(BrowserAction.GoForward, state.pendingAction)
    }

    @Test
    fun `reload should update pendingAction and clear existing error`() {
        val state = BrowserState()
        state.error = BrowserError(-1, "Error", "url")

        state.reload()

        assertEquals(BrowserAction.Reload, state.pendingAction)
        assertNull(state.error)
    }

    @Test
    fun `clearError should set error to null`() {
        val state = BrowserState()
        state.error = BrowserError(-1, "Error", "url")
        state.clearError()
        assertNull(state.error)
    }

    @Test
    fun `consumeAction should set pendingAction to null`() {
        val state = BrowserState(initialUrl = "https://example.com")
        state.consumeAction()
        assertNull(state.pendingAction)
    }

    @Test
    fun `internal setters should update mutable properties`() {
        val state = BrowserState()

        state.url = "https://kotlinlang.org"
        state.title = "Kotlin"
        state.isLoading = true
        state.canGoBack = true
        state.canGoForward = true
        state.error = BrowserError(404, "Not Found", "https://kotlinlang.org")

        assertEquals("https://kotlinlang.org", state.url)
        assertEquals("Kotlin", state.title)
        assertTrue(state.isLoading)
        assertTrue(state.canGoBack)
        assertTrue(state.canGoForward)
        assertNotNull(state.error)
    }

    @Test
    fun `saver should save and restore state correctly`() {
        val originalState = BrowserState(
            initialUrl = "https://example.com",
            initialTitle = "Example Title"
        ).apply {
            canGoBack = true
            canGoForward = true
        }

        val savedBundle = with(BrowserState.Saver) {
            testSaverScope.save(originalState)
        }

        assertNotNull(savedBundle)

        val restoredState = BrowserState.Saver.restore(savedBundle!!)

        assertNotNull(restoredState)
        assertEquals("https://example.com", restoredState?.url)
        assertEquals("Example Title", restoredState?.title)
        assertEquals(true, restoredState?.canGoBack)
        assertEquals(true, restoredState?.canGoForward)
        assertNotNull(restoredState?.savedStateBundle)
    }
}
