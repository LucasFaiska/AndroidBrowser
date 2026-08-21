package com.lucas.oliveira.android.browser.feature.browser

import com.lucas.oliveira.android.browser.core.bridge.BridgePermissionStore
import com.lucas.oliveira.android.browser.core.bridge.PermissionDecision
import com.lucas.oliveira.android.browser.core.url.DeterministicUrlResolver
import com.lucas.oliveira.android.browser.core.url.GoogleSearchEngineProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class BrowserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: BrowserViewModel
    private lateinit var searchEngineProvider: GoogleSearchEngineProvider
    private lateinit var urlResolver: DeterministicUrlResolver
    private lateinit var permissionStore: FakeBridgePermissionStore

    @Before
    fun setup() {
        searchEngineProvider = GoogleSearchEngineProvider()
        urlResolver = DeterministicUrlResolver(searchEngineProvider)
        permissionStore = FakeBridgePermissionStore()
        viewModel = BrowserViewModel(urlResolver, permissionStore)
    }

    @Test
    fun `initial state should have empty address bar`() {
        val uiState = viewModel.uiState.value
        assertEquals("", uiState.addressBarText)
    }

    @Test
    fun `UpdateAddressBar should update addressBarText in uiState`() {
        val newText = "https://kotlinlang.org"
        viewModel.onIntent(BrowserIntent.UpdateAddressBar(newText))

        val uiState = viewModel.uiState.value
        assertEquals(newText, uiState.addressBarText)
    }

    @Test
    fun `SubmitUrl with valid bare domain should update addressBarText and emit LoadUrl effect`() =
        runTest {
            val input = "google.com"
            val expectedResolvedUrl = "https://google.com"

            viewModel.onIntent(BrowserIntent.SubmitUrl(input))

            assertEquals(expectedResolvedUrl, viewModel.uiState.value.addressBarText)

            val effect = viewModel.effect.first()
            assertEquals(BrowserSideEffect.LoadUrl(expectedResolvedUrl), effect)
        }

    @Test
    fun `SubmitUrl with search query should resolve to search engine URL and emit LoadUrl effect`() =
        runTest {
            val input = "android compose navigation"
            val expectedResolvedUrl = searchEngineProvider.buildSearchUrl(input)

            viewModel.onIntent(BrowserIntent.SubmitUrl(input))

            assertEquals(expectedResolvedUrl, viewModel.uiState.value.addressBarText)

            val effect = viewModel.effect.first()
            assertEquals(BrowserSideEffect.LoadUrl(expectedResolvedUrl), effect)
        }

    @Test
    fun `triggering dialog in secureBridgeController updates activeDialog in uiState`() =
        runTest {
            // Directly invoke the controller's onShowDialog callback
            launch {
                viewModel.secureBridgeController.handleInteraction("https://example.com", "myCallback")
            }

            // Advance scheduler to let state update execute
            testScheduler.advanceUntilIdle()

            // Check that activeDialog is updated in state
            val dialogState = viewModel.uiState.value.activeDialog
            assertNotNull(dialogState)
            assertEquals("https://example.com", dialogState?.origin)

            // Simulate responding ALLOWED_ALWAYS
            dialogState?.onResponse?.invoke(PermissionDecision.ALLOWED_ALWAYS)

            testScheduler.advanceUntilIdle()

            // Verify store has the permission
            assertEquals(PermissionDecision.ALLOWED_ALWAYS, permissionStore.getPermission("https://example.com"))
            // Verify dialog state is cleared
            assertNull(viewModel.uiState.value.activeDialog)
        }

    @Test
    fun `onResolveInteraction in secureBridgeController emits EvaluateJavascript side effect`() =
        runTest {
            permissionStore.setPermission("https://example.com", PermissionDecision.ALLOWED_ALWAYS)

            val job = launch {
                val effect = viewModel.effect.first()
                assertTrue(effect is BrowserSideEffect.EvaluateJavascript)
                val script = (effect as BrowserSideEffect.EvaluateJavascript).script
                assertEquals("window.myCallback('{\"appVersion\":\"1.0.0\",\"osVersion\":\"Android 14\",\"batteryLevel\":85}')", script)
            }

            viewModel.secureBridgeController.handleInteraction("https://example.com", "myCallback")
            testScheduler.advanceUntilIdle()
            job.join()
        }

    private class FakeBridgePermissionStore : BridgePermissionStore {
        private val map = mutableMapOf<String, PermissionDecision>()
        override fun getPermission(origin: String): PermissionDecision = map[origin] ?: PermissionDecision.UNDETERMINED
        override fun setPermission(origin: String, decision: PermissionDecision) {
            map[origin] = decision
        }
        override fun clear() {
            map.clear()
        }
    }
}
