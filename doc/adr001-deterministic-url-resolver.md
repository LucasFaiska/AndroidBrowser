# ADR-0001: Deterministic URL Resolver

## Status
Accepted

## Context
The browser address bar must accept arbitrary user input and deterministically classify it as either a direct web URL or a textual search query. The resolution logic must properly handle explicit schemes (`http://`, `https://`), bare domains without schemes (e.g., `example.com`, `subdomain.site.org/path`), local hosts (`localhost`, `127.0.0.1`), and free-form search terms.

## Decision
We implemented a pure-Kotlin `UrlResolver` using a deterministic decision tree and pre-compiled regular expressions:

1. **Sanitization & Fast-Path Whitespace Check:** Input is trimmed. Any string containing internal whitespace (e.g., `android compose flow`) is immediately returned as a search term, bypassing heavy regex evaluation.
2. **Explicit Scheme Detection:** Inputs starting with `http://` or `https://` are treated directly as valid URLs.
3. **Loopback & IP Detection:** Inputs matching `localhost` (with optional port) or valid IPv4 address patterns are prefixed with `http://` and resolved as URLs.
4. **Bare Domain Heuristic:** Inputs matching a standard `domain.tld` structure (requiring a minimum 2-letter alphabetic TLD suffix and valid path/query characters) are prefixed with `https://`.
5. **Fallback to Search Engine:** Any input failing the above checks is treated as a free-text search query.

## Alternatives Considered & Rejected

* **`android.webkit.URLUtil` / `android.util.Patterns.WEB_URL`:** Rejected because they couple domain logic to the Android OS framework, preventing pure JVM unit tests and necessitating Robolectric or instrumented tests.
* **`java.net.URI` / `java.net.URL`:** Rejected because the standard Java URI parser is non-forgiving with unescaped human-entered strings and fails to resolve bare domains without explicit schemes.
* **Pre-navigation Synchronous/Asynchronous DNS Pings:** Rejected to avoid UI latency, network timeouts on ambiguous queries, and DNS privacy leaks for free-text search queries. Connectivity and host resolution failures are handled downstream via `WebViewClient.onReceivedError`.

## Consequences

* **Positive:**
  * Zero Android SDK dependencies in url resolver logic.
  * 100% unit-testable on the JVM with sub-millisecond execution times.
  * Predictable, deterministic behavior with zero perceived UI latency.
  
* **Negative / Trade-offs:**
  * Intranet hostnames without standard TLDs (e.g., `http://corp`) require explicit schemes from the user to avoid falling back to search.