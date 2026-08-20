package com.lucas.oliveira.android.browser.core.url

import org.junit.Assert.assertEquals
import org.junit.Test

class DeterministicUrlResolverTest {

    private val searchEngineProvider = GoogleSearchEngineProvider()
    private val resolver = DeterministicUrlResolver(searchEngineProvider)

    @Test
    fun `test bare domain resolution`() {
        val result = resolver.resolve("  google.com  ")
        assertEquals("https://google.com", result)
    }

    @Test
    fun `test explicit scheme resolution`() {
        val result = resolver.resolve("https://github.com")
        assertEquals("https://github.com", result)
    }

    @Test
    fun `test search query with internal whitespace`() {
        val result = resolver.resolve("android compose flow")
        val expectedUrl = searchEngineProvider.buildSearchUrl("android compose flow")
        assertEquals(expectedUrl, result)
    }

    @Test
    fun `test localhost with port`() {
        val result = resolver.resolve("localhost:8080")
        assertEquals("http://localhost:8080", result)
    }

    @Test
    fun `test ipv4 resolution`() {
        val result = resolver.resolve("127.0.0.1")
        assertEquals("http://127.0.0.1", result)
    }

    @Test
    fun `test fallback to search`() {
        val result = resolver.resolve("example")
        val expectedUrl = searchEngineProvider.buildSearchUrl("example")
        assertEquals(expectedUrl, result)
    }

    @Test
    fun `test subdomain and path resolution`() {
        val result = resolver.resolve("sub.example.com/path?q=1")
        assertEquals("https://sub.example.com/path?q=1", result)
    }

    @Test
    fun `test bare domain with port`() {
        val result = resolver.resolve("example.com:8080")
        assertEquals("https://example.com:8080", result)
    }

    @Test
    fun `test bare domain with port and path`() {
        val result = resolver.resolve("example.com:8080/path")
        assertEquals("https://example.com:8080/path", result)
    }

    @Test
    fun `test empty input`() {
        val result = resolver.resolve("   ")
        val expectedUrl = searchEngineProvider.buildSearchUrl("")
        assertEquals(expectedUrl, result)
    }
}