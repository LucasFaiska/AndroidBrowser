package com.lucas.oliveira.android.browser.core.url

import java.net.URLEncoder
import javax.inject.Inject

/**
 * Strategy contract to format search queries into full URLs.
 */
interface SearchEngineProvider {
    fun buildSearchUrl(query: String): String
}

/**
 * Google implementation.
 */
class GoogleSearchEngineProvider @Inject constructor() : SearchEngineProvider {
    override fun buildSearchUrl(query: String): String {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        return "https://www.google.com/search?q=$encodedQuery"
    }
}
