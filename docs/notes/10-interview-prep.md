# 10 – Interview prep

Practise saying each answer out loud in 2–3 sentences. Don't memorise word for word – understand it.

**First, the honest framing.** If asked how you built it: *"I designed the feature list and built it with an AI coding assistant, working phase by phase. Then I went through every layer myself until I could explain and change it – for example I found that several screens weren't wired into navigation and fixed that."* (Only say the last part once you've done it.) Interviewers increasingly expect AI use; what they test is whether **you** understand the code.

---

1. **Walk me through the architecture.**
Clean Architecture with three layers per feature. Compose screens send Events to a ViewModel that exposes one immutable State as a StateFlow and one-off Effects as a SharedFlow; the ViewModel calls use cases in a pure-Kotlin domain layer, which talk to repository interfaces implemented in the data layer over Room and Retrofit.

2. **What does offline-first mean in your app, concretely?**
Every write goes to Room first and is marked PENDING_INSERT, UPDATE or DELETE, so the app works with no network. A sync manager later pushes pending rows, with deletes kept as hidden tombstones until the server confirms.

3. **How does the UI update when data changes?**
DAOs return Flows; Room re-emits when a table changes; use cases map/combine those Flows; the ViewModel collects them into state; Compose recomposes.

4. **How did you do dependency injection?**
Manually, with container objects and ViewModel factories, built lazily and thread-safely. It's shaped like Hilt modules, so the next step is migrating to Hilt to remove the boilerplate and get compile-time checks.

5. **How does search work without hammering the database?**
The ViewModel emits a query Flow; the use case debounces typing by 250 ms, drops duplicates and uses flatMapLatest to cancel the previous Room query. SQL is built with bound parameters to avoid injection.

6. **How do you handle sync failures and conflicts?**
Transient errors (timeouts, no connectivity, 5xx) retry with exponential backoff up to three times; 4xx fail fast. On a conflict the engine fetches the server copy and applies a strategy – server wins by default, or last-write-wins by timestamp.

7. **Why WorkManager?**
It runs background work reliably even if the app is closed or the phone restarts, respects constraints like "network connected", and handles backoff. I use periodic work for sync and budget alerts and delayed one-time work for the daily reminder.

8. **How does the biometric lock work?**
A process-lifecycle observer locks the app on cold start or after a background timeout. BiometricPrompt's callbacks are wrapped in suspendCancellableCoroutine so the ViewModel can await a result, with a salted, hashed PIN as fallback.

9. **What would you improve about security?**
Use a slow key-derivation function like PBKDF2 or Argon2 instead of a single SHA-256 for passwords and PINs, back secrets with Android Keystore, and add attempt limits. A 4-digit PIN hashed with SHA-256 can be brute-forced instantly if the database is copied.

10. **How do you test ViewModels that use coroutines?**
`runTest`, a MainDispatcherRule that swaps Dispatchers.Main, MockK for dependencies, and Turbine to assert emissions from state and effect Flows.

11. **How do you test Room?**
Robolectric with an in-memory database per test, so DAO queries and Flows run on the JVM without an emulator.

12. **What does your CI do?**
GitHub Actions runs Lint, detekt and ktlint, then unit tests with an HTML report, then debug and release builds; release signing uses secrets when available.

13. **Why store money as Long minor units?**
Floating point can't represent decimals exactly, so 12.50 is stored as 1250 pence to avoid rounding errors.

14. **What's the weakest part of the app?**
Pick one from note 09 and say how you'd fix it – e.g. "Monthly budgets don't reset each month because spent is summed from the start date; I'd calculate it within the current period."

15. **What would you build next?**
Wire every screen into navigation and persist the login session, add a small backend so sync is real, migrate to Hilt, and add tests for the sync engines.

---

### Rapid-fire definitions to know
`StateFlow`, `SharedFlow`, `combine`, `debounce`, `flatMapLatest`, `callbackFlow`, `suspendCancellableCoroutine`, `Mutex`, `viewModelScope`, `Dispatchers.Main/IO/Default`, Room migration, DataStore, WorkManager constraints, `WindowSizeClass`, sealed interface, `@Immutable`, tombstone, exponential backoff.
