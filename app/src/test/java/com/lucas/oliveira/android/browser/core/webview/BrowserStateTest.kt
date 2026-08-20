package com.lucas.oliveira.android.browser.core.webview

import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.saveable.SaverScope
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BrowserStateTest {

    private val testSaverScope = SaverScope { true }
    private lateinit var context: Context
    private lateinit var testWebView: TestWebView

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        testWebView = TestWebView(context)
    }

    @Test
    fun `initial state should match provided values`() {
        val state = BrowserState(initialUrl = "https://example.com")
        assertEquals("https://example.com", state.url)
        assertFalse(state.isLoading)
        assertFalse(state.canGoBack)
        assertFalse(state.canGoForward)
        assertNull(state.error)
    }

    @Test
    fun `default initial state without parameters`() {
        val state = BrowserState()
        assertNull(state.url)
        assertFalse(state.isLoading)
        assertFalse(state.canGoBack)
        assertFalse(state.canGoForward)
        assertNull(state.error)
    }

    @Test
    fun `loadUrl should update url, clear error, and load url in webview`() {
        val state = BrowserState()
        state.webView = testWebView
        state.error = BrowserError(-1, "Error", "url")

        state.loadUrl("https://google.com")

        assertEquals("https://google.com", state.url)
        assertNull(state.error)
        assertEquals("https://google.com", testWebView.lastLoadedUrl)
    }

    @Test
    fun `goBack should trigger webview goBack when canGoBack is true`() {
        val state = BrowserState()
        state.webView = testWebView
        testWebView.mockCanGoBack = true

        state.goBack()

        assertTrue(testWebView.goBackCalled)
    }

    @Test
    fun `goBack should not trigger webview goBack when canGoBack is false`() {
        val state = BrowserState()
        state.webView = testWebView
        testWebView.mockCanGoBack = false

        state.goBack()

        assertFalse(testWebView.goBackCalled)
    }

    @Test
    fun `goForward should trigger webview goForward when canGoForward is true`() {
        val state = BrowserState()
        state.webView = testWebView
        testWebView.mockCanGoForward = true

        state.goForward()

        assertTrue(testWebView.goForwardCalled)
    }

    @Test
    fun `goForward should not trigger webview goForward when canGoForward is false`() {
        val state = BrowserState()
        state.webView = testWebView
        testWebView.mockCanGoForward = false

        state.goForward()

        assertFalse(testWebView.goForwardCalled)
    }

    @Test
    fun `reload should clear error and call reload in webview`() {
        val state = BrowserState()
        state.webView = testWebView
        state.error = BrowserError(-1, "Error", "url")

        state.reload()

        assertNull(state.error)
        assertTrue(testWebView.reloadCalled)
    }

    @Test
    fun `clearError should set error to null`() {
        val state = BrowserState()
        state.error = BrowserError(-1, "Error", "url")
        state.clearError()
        assertNull(state.error)
    }

    @Test
    fun `internal setters should update mutable properties`() {
        val state = BrowserState()

        state.url = "https://kotlinlang.org"
        state.isLoading = true
        state.canGoBack = true
        state.canGoForward = true
        state.error = BrowserError(404, "Not Found", "https://kotlinlang.org")

        assertEquals("https://kotlinlang.org", state.url)
        assertTrue(state.isLoading)
        assertTrue(state.canGoBack)
        assertTrue(state.canGoForward)
        assertNotNull(state.error)
    }

    @Test
    fun `saver should save and restore state correctly`() {
        val originalState = BrowserState(
            initialUrl = "https://example.com"
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
        assertEquals(true, restoredState?.canGoBack)
        assertEquals(true, restoredState?.canGoForward)
        assertNotNull(restoredState?.savedStateBundle)
    }

    private class TestWebView(context: Context) : WebView(context) {
        var lastLoadedUrl: String? = null
        var goBackCalled = false
        var goForwardCalled = false
        var reloadCalled = false
        var mockCanGoBack = false
        var mockCanGoForward = false

        override fun loadUrl(url: String) {
            lastLoadedUrl = url
        }

        override fun goBack() {
            goBackCalled = true
        }

        override fun goForward() {
            goForwardCalled = true
        }

        override fun reload() {
            reloadCalled = true
        }

        override fun canGoBack(): Boolean {
            return mockCanGoBack
        }

        override fun canGoForward(): Boolean {
            return mockCanGoForward
        }
    }
}
