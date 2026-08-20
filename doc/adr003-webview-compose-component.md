# ADR-0003: Encapsulating WebView into a Custom Compose Component

## Status
Accepted

## Context
Embedding a native `WebView` directly into screen layouts mixes low-level view configurations, lifecycle callbacks, and resource management with application UI logic. This causes tight coupling, reduces reusability, and makes state synchronization error-prone.

The application requires an idiomatic and reusable abstraction to render web content cleanly within Jetpack Compose.

## Decision
We decided to build a standalone, reusable Compose component paired with a dedicated UI State Holder, inspired by the design pattern used in standard components like `ScrollState`:

1. **Component Encapsulation:** All low-level native view setup, rendering configurations, and lifecycle cleanup are fully hidden inside a dedicated composable component.
2. **State Holder Pattern:** A dedicated controller class manages the component's internal state, exposes reactive properties for UI observation, and provides direct methods for navigation actions.
3. **Internal Event Translation:** Native browser callbacks are intercepted internally and mapped directly to observable state changes.
4. **Domain Logic Inversion:** Core business rules (such as URL resolution and security validation) are supplied as dependencies, keeping the component focused strictly on rendering and navigation.

## Alternatives Considered & Rejected

* **Inline View Embedding:** Rejected because configuring the view and its listeners directly within screen-level composables violates separation of concerns.
* **Classic MVI Implementation:** Rejected because WebView and WebviewClient have strong dependencies on the Android Context.

## Consequences

* **Positive:**
    * Reusable, declarative component with a clean public API.
    * Complete isolation of platform-specific view management.
    * Direct and predictable state synchronization.
  
* **Negative / Trade-offs:**
    * Component state is bound to the UI composition lifecycle.