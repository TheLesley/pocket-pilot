# Pocket Pilot – Study Notes

Written by reading the actual code (not the project README), 1 Oct 2026.
Goal: be able to explain every part of this app in an interview, without notes.

## How to use these notes
1. Read one note at a time, with the code open next to it in Android Studio.
2. After each note, close it and answer the questions at the bottom out loud. Answers are hidden under "Show answer".
3. Then rebuild one small piece of that feature in **MovieBrowser** from memory, and write the key idea down by hand.
4. Tick the note off below.

## Reading order
| # | Note | What it covers | Done |
|---|------|----------------|------|
| 00 | [Overview](00-overview.md) | What the app does, folder map, every library and why, the big picture | [ ] |
| 01 | [Architecture & the ViewModel pattern](01-architecture.md) | Clean Architecture layers, State/Event/Effect, `UiState`, `Result`, manual DI containers | [ ] |
| 02 | [Transactions](02-transactions.md) | Room entity/DAO, migrations, search with debounce + `flatMapLatest`, safe SQL, validation | [ ] |
| 03 | [Budgets & savings goals](03-budgets-savings.md) | Budget periods, progress maths, savings contributions | [ ] |
| 04 | [Dashboard & analytics](04-dashboard-analytics.md) | `combine`, calculator use cases, Canvas charts, adaptive navigation | [ ] |
| 05 | [Offline-first sync & networking](05-sync-network.md) | Sync status columns, sync engines, `Mutex`, retry/backoff, WorkManager, `callbackFlow` | [ ] |
| 06 | [Auth & app lock](06-auth-security.md) | Local accounts, password/PIN hashing, `AppLockManager`, BiometricPrompt + coroutines | [ ] |
| 07 | [Notifications & settings](07-notifications-settings.md) | DataStore, notification workers, channels, permission | [ ] |
| 08 | [Testing & CI](08-testing-ci.md) | The 90 tests, MockK, Turbine, Robolectric, GitHub Actions pipeline | [ ] |
| 09 | [Known issues & what I'd fix](09-known-issues.md) | **Read before any interview.** Honest list of gaps | [ ] |
| 10 | [Interview prep](10-interview-prep.md) | 15 hard questions with 2–3 sentence answers | [ ] |

## Your study list (concepts beyond Compose/MVVM/Retrofit/Navigation)
- Clean Architecture (presentation / domain / data) and use cases
- MVI-style ViewModel: State, Event, Effect; `StateFlow` vs `SharedFlow`
- Kotlin Flow operators: `map`, `combine`, `debounce`, `distinctUntilChanged`, `flatMapLatest`, `catch`, `onStart`, `launchIn`
- Room: entities, DAOs, `Flow` queries, `@RawQuery`, indices, foreign keys, migrations
- Manual dependency injection (service locators / containers) — and how Hilt would replace it
- Offline-first sync: pending status, tombstones, conflict strategy, exponential backoff
- WorkManager: `CoroutineWorker`, periodic vs one-time work, constraints, custom `WorkerFactory`
- `callbackFlow` and `suspendCancellableCoroutine` (turning callbacks into coroutines)
- DataStore Preferences
- AndroidX Biometric, hashing with salts
- `WindowSizeClass` adaptive layouts
- Compose `Canvas` drawing
- Testing: JUnit 4, MockK, Turbine, `runTest`, `MainDispatcherRule`, Robolectric, Compose UI tests
- CI: GitHub Actions, Android Lint, detekt, ktlint
