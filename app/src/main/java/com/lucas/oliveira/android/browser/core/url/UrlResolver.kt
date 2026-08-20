package com.lucas.oliveira.android.browser.core.url

/**
 * Contract for resolving a user input string into a URL or a search query.
 */
interface UrlResolver {
    /**
     * Resolves the given input into a Url String.
     * @param input The user input string.
     * @return The url as String
     */
    fun resolve(input: String): String
}
