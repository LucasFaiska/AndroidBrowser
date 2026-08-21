package com.lucas.oliveira.android.browser.core.webview

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.test.core.app.ApplicationProvider
import com.lucas.oliveira.android.browser.R
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
class BrowserWebViewClientTest {

    private lateinit var context: Context
    private lateinit var state: BrowserState
    private lateinit var client: BrowserWebViewClient
    private lateinit var webView: TestWebView

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        state = BrowserState()
        client = BrowserWebViewClient(state, context)
        webView = TestWebView(context)
    }

    @Test
    fun `onPageStarted should set isLoading to true, update url, clear error, and update navigation states`() {
        state.isLoading = false
        state.url = null
        state.error = BrowserError(-1, "Error", "url")
        webView.mockCanGoBack = true
        webView.mockCanGoForward = false

        client.onPageStarted(webView, "https://example.com", null)

        assertTrue(state.isLoading)
        assertEquals("https://example.com", state.url)
        assertNull(state.error)
        assertTrue(state.canGoBack)
        assertFalse(state.canGoForward)
    }

    @Test
    fun `doUpdateVisitedHistory should update url and navigation states`() {
        state.url = null
        webView.mockCanGoBack = false
        webView.mockCanGoForward = true

        client.doUpdateVisitedHistory(webView, "https://kotlinlang.org", false)

        assertEquals("https://kotlinlang.org", state.url)
        assertFalse(state.canGoBack)
        assertTrue(state.canGoForward)
    }

    @Test
    fun `onPageFinished should set isLoading to false and update navigation states`() {
        state.isLoading = true
        webView.mockCanGoBack = true
        webView.mockCanGoForward = true

        client.onPageFinished(webView, "https://google.com")

        assertFalse(state.isLoading)
        assertTrue(state.canGoBack)
        assertTrue(state.canGoForward)
    }

    @Test
    fun `onReceivedError should update state error if request is for main frame`() {
        val mockRequest = object : WebResourceRequest {
            override fun getUrl(): Uri = Uri.parse("https://example.com/fail")
            override fun isForMainFrame(): Boolean = true
            override fun isRedirect(): Boolean = false
            override fun hasGesture(): Boolean = false
            override fun getMethod(): String = "GET"
            override fun getRequestHeaders(): Map<String, String> = emptyMap()
        }

        val mockError = android.webkit.TestWebResourceError(-2, "Connection timed out")

        client.onReceivedError(webView, mockRequest, mockError)

        assertNotNull(state.error)
        assertEquals(-2, state.error?.errorCode)
        assertEquals("Connection timed out", state.error?.description)
        assertEquals("https://example.com/fail", state.error?.failingUrl)
    }

    @Test
    fun `onReceivedError should not update state error if request is not for main frame`() {
        val mockRequest = object : WebResourceRequest {
            override fun getUrl(): Uri = Uri.parse("https://example.com/favicon.ico")
            override fun isForMainFrame(): Boolean = false
            override fun isRedirect(): Boolean = false
            override fun hasGesture(): Boolean = false
            override fun getMethod(): String = "GET"
            override fun getRequestHeaders(): Map<String, String> = emptyMap()
        }

        val mockError = android.webkit.TestWebResourceError(404, "Not Found")

        state.error = null

        client.onReceivedError(webView, mockRequest, mockError)

        assertNull(state.error)
    }

    @Test
    fun `onPageCommitVisible should set isRendering to false`() {
        state.isRendering = true

        client.onPageCommitVisible(webView, "https://example.com")

        assertFalse(state.isRendering)
    }

    @Test
    fun `doUpdateVisitedHistory with null url should not overwrite existing url`() {
        state.url = "https://existing.url"

        client.doUpdateVisitedHistory(webView, null, false)

        assertEquals("https://existing.url", state.url)
    }

    @Test
    fun `onReceivedError with ERR_ABORTED error code -3 should early return without setting error`() {
        state.error = null
        state.isRendering = true

        val mockRequest = object : WebResourceRequest {
            override fun getUrl(): Uri = Uri.parse("https://example.com/aborted")
            override fun isForMainFrame(): Boolean = true
            override fun isRedirect(): Boolean = false
            override fun hasGesture(): Boolean = false
            override fun getMethod(): String = "GET"
            override fun getRequestHeaders(): Map<String, String> = emptyMap()
        }

        val mockError = android.webkit.TestWebResourceError(-3, "Some message")

        client.onReceivedError(webView, mockRequest, mockError)

        assertNull(state.error)
        assertTrue(state.isRendering)
    }

    @Test
    fun `onReceivedError with net ERR_ABORTED in description should early return without setting error`() {
        state.error = null
        state.isRendering = true

        val mockRequest = object : WebResourceRequest {
            override fun getUrl(): Uri = Uri.parse("https://example.com/aborted")
            override fun isForMainFrame(): Boolean = true
            override fun isRedirect(): Boolean = false
            override fun hasGesture(): Boolean = false
            override fun getMethod(): String = "GET"
            override fun getRequestHeaders(): Map<String, String> = emptyMap()
        }

        val mockError = android.webkit.TestWebResourceError(-1, "net::ERR_ABORTED")

        client.onReceivedError(webView, mockRequest, mockError)

        assertNull(state.error)
        assertTrue(state.isRendering)
    }

    @Test
    fun `onReceivedError with empty description should fallback to unknown error string`() {
        state.error = null
        state.isRendering = true

        val mockRequest = object : WebResourceRequest {
            override fun getUrl(): Uri = Uri.parse("https://example.com/empty-desc")
            override fun isForMainFrame(): Boolean = true
            override fun isRedirect(): Boolean = false
            override fun hasGesture(): Boolean = false
            override fun getMethod(): String = "GET"
            override fun getRequestHeaders(): Map<String, String> = emptyMap()
        }

        val mockError = android.webkit.TestWebResourceError(-1, "")

        client.onReceivedError(webView, mockRequest, mockError)

        assertNotNull(state.error)
        assertFalse(state.isRendering)
        assertEquals(-1, state.error?.errorCode)
        assertEquals(context.getString(R.string.error_unknown), state.error?.description)
        assertEquals("https://example.com/empty-desc", state.error?.failingUrl)
    }

    private class TestWebView(context: Context) : WebView(context) {
        var mockCanGoBack = false
        var mockCanGoForward = false

        override fun canGoBack(): Boolean {
            return mockCanGoBack
        }

        override fun canGoForward(): Boolean {
            return mockCanGoForward
        }
    }
}
