# ADR-0002: Secure JavaScript Bridge

## Status
Accepted

## Context
Exposing native Android capabilities to a `WebView` via `addJavascriptInterface` introduces significant security vulnerabilities. Malicious or compromised web pages could execute arbitrary native code, access local device APIs, or harvest sensitive application data if native interfaces are bound globally and unrestricted.

The application requires a secure mechanism to execute native JavaScript interfaces while ensuring strict origin verification, runtime user consent, and least-privilege execution.

## Decision
We implemented a secure JavaScript bridge architecture governed by origin inspection and a dynamic runtime consent model:

1. **Origin Verification:** Every JavaScript bridge interaction requires validating the requesting origin against the currently loaded top-level domain.
2. **Runtime User Consent:** Instead of relying solely on hardcoded build-time allowlists, unknown or untrusted domains attempting to invoke native interfaces trigger an explicit UI consent prompt, giving the user granular control over interface execution.
3. **Dynamic Permissions Store:** User decisions (allow/deny) are persisted dynamically in memory or local storage, maintaining a session-aware allowlist/blocklist of authorized origins.
4. **Decoupled Security Layer:** Security validation, origin parsing, and consent evaluation are isolated in pure domain components, independent of the `WebView` lifecycle, allowing comprehensive unit testing.
5. **Least-Privilege API Surface:** Exposed native methods are restricted to strictly necessary operations, annotated with `@JavascriptInterface`, and sanitized to reject malformed payloads.

## Alternatives Considered & Rejected

* **Static Build-Time Allowlist:** Rejected because hardcoding approved domains restricts flexibility and prevents runtime adaptability for dynamic web environments.
* **Unrestricted Bridge Binding:** Rejected due to high security risks of unauthorized arbitrary code execution by untrusted third-party web content.

## Consequences

* **Positive:**
    * Robust defense against malicious script injection and unauthorized native invocation.
    * Granular user control via runtime permission prompts.
    * Domain security rules are isolated, deterministic, and testable on the JVM.
* **Negative / Trade-offs:**
    * Additional UI flow required to handle runtime permission dialogs.
    * Requires managing and persisting dynamic permission states across browsing sessions.