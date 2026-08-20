package com.lucas.oliveira.android.browser.urlresolver

/**
 * Contract for resolving a user input string into a URL or a search query.
 */
interface UrlResolver {
    /**
     * Resolves the given input into a [UrlResult].
     * @param input The user input string.
     * @return A [UrlResult.Url] if the input is classified as a URL, or [UrlResult.Search] otherwise.
     */
    fun resolve(input: String): UrlResult
}
