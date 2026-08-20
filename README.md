# Infinity Browser - Android Take-Home Assignment

---

## 1. What was built

---

## 2. Key Architectural Decisions & Trade-offs

To describe the Architectural Decisions & Trade-offs I chose the ADR documentation strategy.
The ADRs can be read here:
* [ADR-0001: Deterministic URL Resolver](doc/adr001-deterministic-url-resolver.md)
* [ADR-0002: Secure JavaScript Bridge and Runtime Consent Model](doc/adr002-secure-javascript-bridge.md)
* [ADR-0003: Reactive WebView Encapsulation and Lifecycle Management](doc/adr003-webview-compose-component.md)

---

## 3. What was deliberately left out and why


---

## 4. How AI Tools were used

### Planning

After reading the test problem statement I defined 3 main architecture constraints based on the test requirements which are: URL resolution, Javascript Bridge security and UI state management.

Considering these architecture constraints I used the Gemini LLM tool to help in the ADRs creation.

In each ADR creation I provided some concepts that I wanted to follow, for example: scalability, simplicity, performance, cohesion, decoupling from Android SDK and other important core concepts.  

#### ADR0001
Gemini proposed an ADR aligned with what I had in mind, so I asked for a draft of this ADR and the final version only has some small adjustments.

#### ADR0002
This ADR had the deepest discussion between me and Gemini. AI's first suggestion was a static URL allowlist defined at build time to allow the Javascript Bridge to run, but I discarded this idea and asked to refine it with a UI prompt solution and a dynamic allowlist. The final version of this ADR is the result of AI suggestion but with my requirements. 

#### ADR0003
Gemini suggested the classic MVI architecture using an asynchronous mediator bridge and command channels to connect a ViewModel to the WebView. I challenged this approach bringing to the discussion the native Compose patterns, specifically drawing a parallel with foundation state holders like `ScrollState`. This directed the final solution: encapsulating the WebView into a Compose component with a dedicated UI State Holder.

### Implementation

During the coding phase, I used the Android Studio Gemini Assistant strictly as an accelerator to scaffold classes, data structures, and unit tests.

While the ADRs served as the architectural blueprints, I maintained a strict, spec-first approach for the implementation:
* **Contract Specification:** Explicitly defined the required class contracts, sealed hierarchies, immutability constraints, and dependency injection boundaries before prompting code generation.
* **Iterative Refinement & Curation:** Continuously refined and iterated over the generated code across multiple passes, approving only implementations that strictly matched the predefined mental model, domain contracts, and Kotlin idioms.
* **JVM Test Coverage:** Leveraged the assistant to rapidly expand edge-case coverage (e.g., scheme validation, special characters, localhost resolution), validating and adjusting all test suites locally.

---