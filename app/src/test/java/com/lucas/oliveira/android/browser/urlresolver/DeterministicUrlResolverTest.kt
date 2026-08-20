package com.lucas.oliveira.android.browser.urlresolver

import org.junit.Assert.assertEquals
import org.junit.Test

class DeterministicUrlResolverTest {

    private val resolver = DeterministicUrlResolver()

    @Test
    fun `test bare domain resolution`() {
        val result = resolver.resolve("  google.com  ")
        assertEquals(UrlResult.Url("https://google.com"), result)
    }

    @Test
    fun `test explicit scheme resolution`() {
        val result = resolver.resolve("https://github.com")
        assertEquals(UrlResult.Url("https://github.com"), result)
    }

    @Test
    fun `test search query with internal whitespace`() {
        val result = resolver.resolve("android compose flow")
        assertEquals(UrlResult.Search("android compose flow"), result)
    }

    @Test
    fun `test localhost with port`() {
        val result = resolver.resolve("localhost:8080")
        assertEquals(UrlResult.Url("http://localhost:8080"), result)
    }

    @Test
    fun `test ipv4 resolution`() {
        val result = resolver.resolve("127.0.0.1")
        assertEquals(UrlResult.Url("http://127.0.0.1"), result)
    }

    @Test
    fun `test fallback to search`() {
        val result = resolver.resolve("example")
        assertEquals(UrlResult.Search("example"), result)
    }

    @Test
    fun `test subdomain and path resolution`() {
        val result = resolver.resolve("sub.example.com/path?q=1")
        assertEquals(UrlResult.Url("https://sub.example.com/path?q=1"), result)
    }

    @Test
    fun `test bare domain with port`() {
        val result = resolver.resolve("example.com:8080")
        assertEquals(UrlResult.Url("https://example.com:8080"), result)
    }

    @Test
    fun `test bare domain with port and path`() {
        val result = resolver.resolve("example.com:8080/path")
        assertEquals(UrlResult.Url("https://example.com:8080/path"), result)
    }

    @Test
    fun `test empty input`() {
        val result = resolver.resolve("   ")
        assertEquals(UrlResult.Search(""), result)
    }
}
