# ✈️ PocketPilot

![Platform](https://img.shields.io/badge/Platform-Android_15_(API_35)-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

PocketPilot is a native Android personal finance manager built to log spending, set monthly category budgets, and analyze financial behavior. It operates offline-first using Room persistence and offers optional biometric authentication (fingerprint/face unlock).

---

## ✨ Key Features

- **Dashboard:** Overview of total balance, monthly income vs. expenses, and recent logs.
- **Transactions:** Full CRUD support with category tagging, search, and date filters.
- **Budgeting:** Monthly category limits with visual progress indicators.
- **Analytics:** Category distribution breakdown charts.
- **Security:** App lock with AndroidX Biometric (fingerprint/face) and a salted, hashed PIN fallback.
- **Adaptive UI:** Supports compact phones, foldables, and tablets via `WindowSizeClass`.

---

## 🎨 Design System

PocketPilot features a "Dark Fintech" aesthetic:
- **Background / Surfaces:** Dark Slate (`#0F1117` base / `#191C24` cards)
- **Brand Primary:** Indigo (`#6366F1`)
- **Semantic Indicators:** Mint Green (`#34D399`) for income, Coral Red (`#FB7185`) for expenses, Cyan (`#22D3E## 🏗️ Tech Stack & Architecture

Built with **Clean Architecture** (presentation / domain / data per feature) and **Unidirectional Data Flow**: each ViewModel exposes one immutable `StateFlow` state, accepts sealed `Event`s and emits one-off `Effect`s.

- **UI:** Jetpack Compose, Material Design 3, `WindowSizeClass` adaptive shell, custom Canvas charts
- **Navigation:** type-safe, hand-written navigation with sealed destination classes and `AnimatedContent`
- **State & async:** ViewModel, Coroutines, Flow / StateFlow / SharedFlow
- **Dependency injection:** manual DI – per-feature container objects and `ViewModelProvider.Factory` classes, structured to map one-to-one onto Hilt modules
- **Data & storage:** Room (SQLite, migrations v1→v4, indexed queries), DataStore Preferences
- **Networking:** Retrofit, OkHttp, kotlinx.serialization, typed `NetworkException` mapping
- **Background work:** WorkManager (periodic sync, budget alerts, daily reminder) with a custom `WorkerFactory`
- **Security:** AndroidX Biometric, salted SHA-256 PIN/password hashing
- **Testing:** JUnit 4, MockK, Turbine, kotlinx-coroutines-test, Robolectric (Room DAO + Compose UI tests)
- **Quality gates:** Android Lint, detekt, ktlint, GitHub Actions CI

### Project status
- ✅ Auth, transactions (CRUD, search, filters, sort) and dashboard are live in the app.
- 🚧 Budgets, savings goals, analytics, settings and app-lock setup screens are implemented but not yet linked into the main navigation.
- 🚧 The sync layer is complete on the client side; no backend is deployed yet (`NetworkConfig.remoteBackendUrl` is `null`).

### Project structure

```text
app/src/main/java/com/example/pocketpilot/
 ├── PocketPilotApplication.kt   # Builds the dependency graph, schedules background work
 ├── MainActivity.kt             # App lock gate → auth flow → adaptive home shell
 ├── core/
 │    ├── common/                # Result type
 │    ├── ui/                    # BaseViewModel (State/Event/Effect), UiState
 │    ├── designsystem/          # Theme tokens + shared components
 │    ├── network/               # Retrofit/OkHttp setup, safeApiCall, NetworkException
 │    └── sync/                  # SyncManager, NetworkMonitor, WorkManager workers
 └── feature/
      ├── auth/ dashboard/ finance/ analytics/
      └── security/ settings/ notifications/
           # each: presentation/ · domain/ (models, repositories, use cases) · data/ · di/
```

Study notes explaining every part of the code live in [`docs/notes/`](docs/notes/README.md).
