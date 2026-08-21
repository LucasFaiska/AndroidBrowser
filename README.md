# Infinity Browser - Android Assignment

## 1. What was built

A single-tab Android browser built with: **Kotlin, Jetpack Compose, Coroutines/Flows, MVI/MVVM, and Dagger Hilt**.

### Key Features:
* **Deterministic URL & Search Resolution:** A pure-Kotlin resolver (`DeterministicUrlResolver`) that resolves bare domains, `localhost`, IPv4, scheme-prefixed URLs, and search queries (routed to a Search Engine) without relying on Android SDK or Java Libraries dependencies.
* **Navigation & History Controls:** Back, forward, and reload operations synchronized directly with the underlying `WebView` back-forward list.
* **Browser View Component:** A component to encapsulate the `WebView` and `WebViewClient` inspired by native Compose stateful components (like `LazyListState`).
* **Secure JavaScript Bridge:** An origin-verified native bridge (`AndroidBridgeNative`) exposing device metadata (`appVersion`, `osVersion`, `batteryLevel`). Access is gated behind explicit user consent, supporting ephemeral session authorization (`Allow Once`) via in-memory storage and persistent rules (`Always Allow`, `Deny`) backed by `SharedPreferences`.
* **Testing Strategy:** 55 unit and integration tests executing on the JVM (covering URL resolution, bridge permission states, ViewModel effects, and Compose UI via Robolectric).

> **Live Test Page:** https://sparkling-feather-c0b4.lucas-faiskaa.workers.dev/

---

## 2. Key Architectural Decisions & Trade-offs

Detailed architectural trade-offs are documented via ADRs:
* [ADR-0001: Deterministic URL Resolver](doc/adr001-deterministic-url-resolver.md)
* [ADR-0002: Secure JavaScript Bridge and Runtime Consent Model](doc/adr002-secure-javascript-bridge.md)
* [ADR-0003: Reactive WebView Encapsulation and Lifecycle Management](doc/adr003-webview-compose-component.md)

---

## 3. What was deliberately left out and why

To adhere to the recommended **4-hour time budget**, all optional stretch goals were deliberately omitted. Effort was prioritized on the runtime stability, security, and testability of the core browsing loop:

* **Scope Discipline:** Focused on security boundary validation, thread synchronization between WebKit and Coroutines, and edge-case handling rather than shallow UI feature accumulation.
* **Lifecycle & State Restoration:** Handled Android configuration changes by implementing a dedicated `Saver` for `BrowserState`, preserving the native `WebView` navigation stack across Activity recreations.
* **Extensibility Considerations:** Features like Bookmarks or History require dedicated local persistence (e.g., Room) and synchronization around web redirects. Multi-window/Tabs introduces complex `WebView` instance pooling and memory overhead. These were scoped out to keep the single-session core isolated and leak-free.

---

## 4. How AI Tools were used

AI (Gemini) was used during architectural planning and code scaffolding, functioning as a sounding board while keeping design decisions spec-driven:

### Architectural Planning & ADRs
* **ADR-0001 (URL Resolution):** Used Gemini to draft the initial Regex baseline for domain and IP recognition, then refined it to ensure zero coupling with the Android SDK for fast JVM unit testing.
* **ADR-0002 (Bridge Security):** Rejected the initial AI proposal of a static build-time URL allowlist. Steered the design toward an Origin-based runtime prompt model supporting both ephemeral (`ALLOWED_ONCE`) and persistent permissions.
* **ADR-0003 (WebView in Compose):** Rejected a generic MVI mediator channel in favor of standard Compose patterns, structuring a dedicated `BrowserState` holder (analogous to `ScrollState`) to manage `WebView` lifecycle and avoid memory leaks in the UI tree.

### Implementation & Scaffolding
* **Boilerplate & Test Generation:** Used Android Studio's assistant to scaffold data classes, Hilt bindings, and expand edge-case coverage in unit test suites.
* **Active Refactoring & Curation:** Filtered out overengineered abstractions (such as unnecessary wrapper classes and generic composable slots) to keep class contracts cohesive, explicit, and idiomatic to modern Kotlin and Android development.