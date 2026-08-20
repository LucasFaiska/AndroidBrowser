package com.lucas.oliveira.android.browser.urlresolver

/**
 * Sealed class representing the result of a URL resolution.
 */
sealed class UrlResult {
    /**
     * Represents a valid URL result.
     * @param value The resolved URL string (including scheme).
     */
    data class Url(val value: String) : UrlResult()

    /**
     * Represents a search query result.
     * @param query The search query string.
     */
    data class Search(val query: String) : UrlResult()
}
