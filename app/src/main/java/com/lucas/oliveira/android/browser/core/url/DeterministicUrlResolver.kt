package com.lucas.oliveira.android.browser.core.url

import java.util.regex.Pattern
import javax.inject.Inject

/**
 * Deterministic implementation of [UrlResolver] based on ADR-0001.
 */
class DeterministicUrlResolver @Inject constructor(
    private val searchEngineProvider: SearchEngineProvider
) : UrlResolver {

    companion object {
        private val EXPLICIT_SCHEME = Pattern.compile("^(http|https)://.*", Pattern.CASE_INSENSITIVE)
        private val LOCALHOST = Pattern.compile("^localhost(:\\d+)?(/.*)?$", Pattern.CASE_INSENSITIVE)
        private val IPV4 = Pattern.compile("^(\\d{1,3}\\.){3}\\d{1,3}(:\\d+)?(/.*)?$")
        private val BARE_DOMAIN = Pattern.compile("^[a-z0-9-]+(\\.[a-z0-9-]+)*\\.[a-z]{2,}(:\\d+)?(/.*)?$", Pattern.CASE_INSENSITIVE)
    }

    override fun resolve(input: String): String {
        val trimmed = input.trim()
        
        if (trimmed.isEmpty()) {
            return searchEngineProvider.buildSearchUrl("")
        }

        if (trimmed.any { it.isWhitespace() }) {
            return searchEngineProvider.buildSearchUrl(trimmed)
        }

        if (EXPLICIT_SCHEME.matcher(trimmed).matches()) {
            return trimmed
        }

        if (LOCALHOST.matcher(trimmed).matches() || IPV4.matcher(trimmed).matches()) {
            return "http://$trimmed"
        }

        if (BARE_DOMAIN.matcher(trimmed).matches()) {
            return "https://$trimmed"
        }

        return searchEngineProvider.buildSearchUrl(trimmed)
    }
}
