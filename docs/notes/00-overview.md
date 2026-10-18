# 00 – Overview

## What the app is
Pocket Pilot is an **offline-first personal finance app**. You sign up / log in, then you can:
- log income and expense **transactions** (add, edit, delete, search, filter, sort)
- see a **dashboard**: balance, this month's income vs spending, monthly budget, spending by category, recent transactions

The code also contains **budgets**, **savings goals**, **analytics charts**, **settings**, **notification settings** and a **PIN / fingerprint app lock** — all built and partly tested — but **those screens are not yet reachable from the app's navigation** (only the Dashboard and Transactions tabs are wired up). See [09 – Known issues](09-known-issues.md). This is the single most important thing to fix before an interviewer installs the app.

"Offline-first" means: every write goes to the local database first, so the app works with no internet. A sync layer is built to upload changes to a server later — but **there is no real server yet** (`NetworkConfig.remoteBackendUrl = null`).

Size: ~290 Kotlin files, ~19,700 lines in `app/src/main`, 90 unit tests.

## Folder map (`app/src/main/java/com/example/pocketpilot/`)
```
PocketPilotApplication.kt   App start-up: builds all the containers, schedules background work
MainActivity.kt             Hosts Compose. AppLockGate → login flow → adaptive shell (Dashboard / Transactions)
core/
  common/Result.kt          Success / Error / Loading wrapper + helpers
  ui/                       BaseViewModel (State/Event/Effect), UiState
  designsystem/             Theme (colours, type, shapes, spacing) + shared components
                            (AdaptiveNavScaffold, buttons, text fields, SyncStatusBar…)
  network/                  Retrofit/OkHttp setup, auth header, safeApiCall, NetworkException types
  sync/                     SyncManager, NetworkMonitor, WorkManager workers & scheduler
feature/
  auth/          sign up, login, forgot/reset password (accounts stored locally in Room)
  finance/       transactions, budgets, savings goals: Room DB, repositories, sync engines, screens
  dashboard/     summary screen + the HomeNavHost (the app's main navigation)
  analytics/     charts: cash flow, category donut, trends (not yet reachable)
  security/      PIN + biometric app lock (not yet switch-on-able from the UI)
  settings/      theme, currency, data management (not yet reachable)
  notifications/ budget alerts + daily reminder workers and settings (screen not reachable)
```
Every feature uses the same three layers:
```
presentation/  Compose screens + ViewModels + "Contract" (State, Event, Effect)
domain/        plain Kotlin: models, repository interfaces, use cases, validators
data/          Room entities/DAOs, Retrofit APIs/DTOs, mappers, repository implementations
di/            "Container" objects that build everything (manual dependency injection)
```

## Every library and why it's here (from `app/build.gradle.kts`)
| Library | Used for |
|---|---|
| Jetpack Compose + Material 3 | All UI |
| material3-window-size-class | Choosing bottom bar / rail / drawer by screen width |
| Lifecycle ViewModel / runtime-compose | ViewModels, `collectAsStateWithLifecycle` |
| lifecycle-process | `ProcessLifecycleOwner` – knows when the whole app goes to background (for auto-lock) |
| Coroutines | All async work |
| Room (+ KSP compiler) | Local SQLite database |
| DataStore Preferences | Small key-value settings (theme, currency, lock settings, notification prefs) |
| Retrofit + OkHttp + logging interceptor | Network layer for sync (no live server yet) |
| kotlinx.serialization | JSON ↔ Kotlin for Retrofit DTOs |
| WorkManager | Background sync every 15 min, budget alerts, daily reminder |
| AndroidX Biometric | Fingerprint / face unlock |
| JUnit 4, MockK, Turbine, coroutines-test, Robolectric, Room testing, Compose UI test | Tests |
| detekt, ktlint (Gradle plugins) | Code-style and static analysis, run in CI |

**Not actually used, despite what the old README said:** Hilt (it's in `libs.versions.toml` but never added to the app — DI is manual), Navigation Compose (navigation is hand-written with sealed classes), KeyStore, Paging, JUnit 5.

## The big picture: what happens when you add an expense
1. `TransactionFormScreen` sends an Event (e.g. `Save`) to `TransactionFormViewModel`.
2. The ViewModel validates fields with `TransactionValidator`, converts "12.50" to `1250` minor units, builds a `Transaction`, and calls `AddTransactionUseCase`.
3. The use case calls `TransactionRepository.upsert(...)` (an interface in `domain/`).
4. `TransactionRepositoryImpl` (in `data/`) writes a `TransactionEntity` to Room with `syncStatus = PENDING_INSERT`.
5. Room notices the table changed and re-emits every `Flow` that reads it — so the Dashboard and the list update **automatically**, no manual refresh.
6. Later, `SyncManager` (triggered by WorkManager every 15 min, or when the phone comes back online) would upload pending rows — currently it fails quietly because there's no server.

## App start-up order (`PocketPilotApplication.onCreate`)
1. `FinanceContainer.init` – creates the Room database (and the sync graph).
2. `AuthContainer`, `SettingsContainer`, `SecurityContainer` init.
3. `AppLockManager.start()` – starts watching app foreground/background.
4. Worker factories created, then periodic sync + budget-alert work is scheduled.
5. Daily reminder rescheduled from saved preferences.

The class also implements `Configuration.Provider` so WorkManager uses **its** worker factory (needed because workers have constructor parameters). The default WorkManager initializer is disabled in `AndroidManifest.xml` for the same reason.

## Questions
1. In one sentence, what does "offline-first" mean in this app?
<details><summary>Show answer</summary>Every change is saved to the local Room database first, so the app works without internet; a sync layer uploads changes to a server later.</details>

2. Name the three layers every feature is split into and what goes in each.
<details><summary>Show answer</summary>presentation (Compose screens, ViewModels, State/Event/Effect), domain (plain Kotlin models, repository interfaces, use cases, validators), data (Room, Retrofit, mappers, repository implementations).</details>

3. Why does the Dashboard update by itself after you add a transaction?
<details><summary>Show answer</summary>The DAO returns a `Flow`; Room re-emits it whenever the `transactions` table changes, and the use case/ViewModel collect that Flow into state.</details>

4. Why is money stored as `amountMinorUnits: Long` and not a `Double`?
<details><summary>Show answer</summary>Doubles can't represent many decimals exactly (0.1 + 0.2 ≠ 0.3). Storing pence/cents as whole numbers avoids rounding errors.</details>

5. Why does `PocketPilotApplication` implement `Configuration.Provider`?
<details><summary>Show answer</summary>So WorkManager is built with the app's custom `WorkerFactory`, which can pass dependencies (like the SyncManager) into worker constructors.</details>

6. Which libraries does the app NOT use even though the old README claimed it did?
<details><summary>Show answer</summary>Hilt, Navigation Compose, KeyStore, JUnit 5 (it uses JUnit 4).</details>
