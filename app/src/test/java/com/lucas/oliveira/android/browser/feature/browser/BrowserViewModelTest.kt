package com.lucas.oliveira.android.browser.feature.browser

import com.lucas.oliveira.android.browser.core.url.DeterministicUrlResolver
import com.lucas.oliveira.android.browser.core.url.GoogleSearchEngineProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
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

    @Before
    fun setup() {
        searchEngineProvider = GoogleSearchEngineProvider()
        urlResolver = DeterministicUrlResolver(searchEngineProvider)
        viewModel = BrowserViewModel(urlResolver)
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
}