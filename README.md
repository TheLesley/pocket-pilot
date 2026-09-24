<div align="center">

# PocketPilot

**A production-grade native Android personal-finance app built with Kotlin, Jetpack Compose, and Clean Architecture.**

[![Android CI](https://github.com/ihechinkwocha/PocketPilot/actions/workflows/android_ci.yml/badge.svg?branch=main)](https://github.com/ihechinkwocha/PocketPilot/actions/workflows/android_ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-min%2024%20%7C%20target%2036-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026.02-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%203-Design-757575?logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

</div>

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Tech Stack](#tech-stack)
3. [Clean Architecture & Modularization](#clean-architecture--modularization)
4. [Key Features](#key-features)
5. [UI Screenshots](#ui-screenshots)
6. [Setup & Build Instructions](#setup--build-instructions)
7. [Gradle Tasks Cheat Sheet](#gradle-tasks-cheat-sheet)
8. [Continuous Integration & Delivery](#continuous-integration--delivery)
9. [Testing & Quality](#testing--quality)
10. [Project Roadmap](#project-roadmap)
11. [License](#license)

---

## Project Overview

**PocketPilot** is a modern, offline-first personal finance tracker for Android. It lets a user log
transactions, set monthly budgets, plan savings goals, receive smart budget alerts, and secure the
whole thing behind biometric + PIN app-lock — all while working smoothly with or without a network
connection.

The project is intentionally engineered to demonstrate **junior/graduate Android engineering
craft** end-to-end:

- **Modern architecture**: MVVM + Clean Architecture + Unidirectional Data Flow (UDF).
- **Offline-first data layer**: Room as the single source of truth; Retrofit + WorkManager for
  background sync.
- **Reactive UI**: 100% Jetpack Compose with `StateFlow`-driven screens.
- **Production hygiene**: KtLint + Detekt + Android Lint with baselines, GitHub Actions CI,
  signed release AAB builds, minification & resource shrinking, ProGuard/R8 rules.
- **Test discipline**: JUnit4 + MockK + Turbine + Robolectric + Room in-memory tests across use
  cases, ViewModels, DAOs, and Compose UI.

It is designed to be **read as a portfolio**: every layer is discoverable from the top of the
package tree, and every module is small enough to reason about in a single sitting.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | **Kotlin 2.2** (K2 compiler) |
| UI Toolkit | **Jetpack Compose** (BOM 2026.02), **Material 3**, Material Icons Extended |
| Adaptive UI | Material 3 `WindowSizeClass` (compact / medium / expanded layouts) |
| Architecture | **MVVM + Clean Architecture**, Unidirectional Data Flow |
| Async & Streams | **Kotlin Coroutines**, `Flow`, `StateFlow` |
| Dependency Injection | Lightweight **manual DI containers** (per-feature `*Container` objects), constructor injection everywhere |
| Local Persistence | **Room 2.7** (KSP), **DataStore Preferences** |
| Networking | **Retrofit 2.11** + **OkHttp 4.12** + `kotlinx-serialization-converter` |
| Serialization | **kotlinx.serialization** |
| Background Work | **WorkManager 2.10** with a `DelegatingWorkerFactory` for sync + notification workers |
| Security | **AndroidX Biometric** (BiometricPrompt) + SHA-256 salted PIN, `AppLockManager` gated on process lifecycle |
| Notifications | Native `NotificationManagerCompat`, budget-threshold + daily-reminder workers |
| Java Time | Core library desugaring (`java.time` on API 24+) |
| Build System | **Gradle Kotlin DSL** with **Version Catalogs** (`libs.versions.toml`), AGP 9.2 |
| Static Analysis | **KtLint 1.5**, **Detekt 1.23**, **Android Lint** (all with baselines) |
| Testing | JUnit 4, **MockK**, **Turbine**, **Robolectric**, `kotlinx-coroutines-test`, `androidx.room:room-testing`, Compose UI test |
| CI/CD | **GitHub Actions** — static analysis, unit tests, signed `assembleRelease` + `bundleRelease` on `main` / tags |

---

## Clean Architecture & Modularization

PocketPilot follows a **feature-first package structure** inside a single Gradle module (`:app`),
with each feature internally split into `domain / data / presentation / di` — the same split you
would see in a fully multi-module app. This keeps the project navigable while making a future
module extraction mechanical.

### High-level layering

```
┌──────────────────────────────────────────────────────────────────────┐
│                          Presentation (UI)                           │
│   Jetpack Compose screens · ViewModels · StateFlow · Navigation      │
│                                                                      │
│   ▲                                                                  │
│   │ UiState / UiEvent (UDF)                                          │
│   │                                                                  │
├──────────────────────────────────────────────────────────────────────┤
│                             Domain                                   │
│   Use Cases · Domain Models · Validators · Repository Interfaces     │
│                                                                      │
│   ▲                                                                  │
│   │ Pure Kotlin — no Android, no Room, no Retrofit                   │
│   │                                                                  │
├──────────────────────────────────────────────────────────────────────┤
│                              Data                                    │
│   Repository impls · Mappers · DTOs · Room Entities · WorkManager    │
│                                                                      │
│      ▲                              ▲                                │
│      │                              │                                │
│  ┌───┴─────────┐              ┌─────┴────────┐                       │
│  │   Room DB   │◀── source ── │   Retrofit   │                       │
│  │  (offline   │  of truth    │   (remote    │                       │
│  │   first)    │              │    API)      │                       │
│  └─────────────┘              └──────────────┘                       │
└──────────────────────────────────────────────────────────────────────┘
```

**Guardrails enforced across the codebase**

- UI never touches Room or Retrofit directly — only ViewModels via Use Cases.
- DTO ⇄ Entity ⇄ Domain Model conversions live in explicit `mapper/` packages.
- Repositories return `Flow` from Room; remote fetches feed into Room and downstream observers
  update automatically.
- All ViewModel-facing state uses `StateFlow<UiState>`; screens are driven with
  `collectAsStateWithLifecycle`.
- WorkManager is the only place background sync runs; the app is functional offline.

### Package structure

```
com.example.pocketpilot
├── core
│   ├── common          # shared kotlin utilities
│   ├── designsystem    # PocketPilot Compose design system (theme, components)
│   ├── network         # Retrofit / OkHttp wiring, NetworkConfig
│   ├── sync            # SyncManager, connectivity observer, work status
│   └── ui              # Base UI primitives (BaseViewModel, adaptive scaffolds)
│
├── feature
│   ├── auth            # Login / Signup / Forgot & Reset Password
│   ├── dashboard       # Home dashboard: balance, monthly totals, category breakdown
│   ├── finance         # Transactions, Budgets, Savings Goals (CRUD + list + detail + form)
│   ├── analytics       # Trends, breakdown, overview visualisations
│   ├── notifications   # Budget alerts, daily reminder scheduling
│   ├── security        # App-lock, PIN hashing, biometric prompt
│   └── settings        # Currency, theme, data preferences
│
├── MainActivity.kt           # Root Compose graph, adaptive navigation, sync status bar
└── PocketPilotApplication.kt # Boot ordering for DI containers + WorkManager
```

Each feature package follows the same recursive shape:

```
feature/<name>
├── data
│   ├── local        # Room entities, DAOs, converters
│   ├── mapper       # DTO/Entity ⇄ Domain
│   ├── remote       # Retrofit APIs + DTOs
│   └── repository   # Repository implementations
├── di               # <Name>Container — manual DI graph for the feature
├── domain
│   ├── model        # Immutable domain models
│   ├── repository   # Repository interfaces
│   ├── usecase      # Single-purpose use cases
│   └── validation   # Pure validators (unit-testable)
└── presentation     # Compose screens + ViewModels
```

> **Why manual DI containers instead of Hilt?**  
> The feature-scoped `*Container` objects (`FinanceContainer`, `SecurityContainer`,
> `NotificationsContainer`, …) give the same wiring properties as a DI graph while keeping the
> object graph obvious in code review — every dependency is one Ctrl+Click away. This is a
> deliberate portfolio choice; migrating to Hilt is a mechanical follow-up (the version catalog
> already tracks Hilt aliases).

---

## Key Features

- 💸 **Transactions** — add, edit, delete, search, and browse expenses/income with Room-backed
  offline storage. Category-aware forms with domain-level validation.
- 📊 **Dashboard** — running balance, month-to-date totals, category breakdown pie/bar, and quick
  navigation to full analytics.
- 🎯 **Budgets** — monthly budgets per category with live "spent vs. remaining" calculations and
  color-coded progress.
- 🐷 **Savings Goals** — track targets, contribute or withdraw, and visualise progress toward each
  goal.
- 📈 **Analytics** — trends over time, per-category breakdown, and overview widgets built on top
  of the same Room source of truth.
- 🔔 **Smart Notifications** — WorkManager-scheduled **budget alerts** when spend crosses
  thresholds, plus an optional **daily reminder** with reschedule-on-boot handling.
- 🔒 **App Lock & Security** — `AppLockManager` gates the whole UI on process resume, backed by
  a SHA-256 salted PIN and AndroidX **BiometricPrompt**.
- 🔄 **Offline-First Sync** — Room is the single source of truth; a periodic + connectivity-driven
  `SyncManager` flushes pending changes when the network returns. A sticky top-bar surfaces sync
  status and a **Sync Now** action.
- 🎨 **Adaptive Material 3 UI** — dark/light theme, dynamic color where available, and
  `WindowSizeClass`-driven layouts for phones, foldables, and tablets.
- 🌐 **Settings** — currency, theme, and data preferences persisted with DataStore.
- 🧪 **Test-first** — every use case, ViewModel, DAO, and validator has a corresponding unit
  test; core Compose components are exercised with the Compose UI test harness under Robolectric.

---

## UI Screenshots

> Screenshots and animated demos will be added under `docs/screenshots/` before public release.
> The placeholders below are intentional and reserve the layout for the recruitment README.

<div align="center">

| Dashboard | Transactions | Budgets |
|:---:|:---:|:---:|
| _`docs/screenshots/dashboard.png`_ | _`docs/screenshots/transactions.png`_ | _`docs/screenshots/budgets.png`_ |

| Savings | Analytics | App Lock |
|:---:|:---:|:---:|
| _`docs/screenshots/savings.png`_ | _`docs/screenshots/analytics.png`_ | _`docs/screenshots/app-lock.png`_ |

**Live demo GIF:** _`docs/screenshots/pocketpilot-demo.gif`_

</div>

---

## Setup & Build Instructions

### Prerequisites

| Tool | Version |
|---|---|
| JDK | **17** (Temurin recommended — matches the CI toolchain) |
| Android SDK | Platform **36** installed, Build Tools 34+ |
| Android Studio | **Ladybug (2024.2)** or newer |
| Gradle | Wrapped — no local install required (`gradlew` / `gradlew.bat`) |

### 1. Clone

```bash
git clone https://github.com/ihechinkwocha/PocketPilot.git
cd PocketPilot
```

### 2. Configure the SDK location

Create a `local.properties` file at the repo root (it is git-ignored) pointing at your Android
SDK:

```properties
# Windows
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk

# macOS
sdk.dir=/Users/<you>/Library/Android/sdk

# Linux
sdk.dir=/home/<you>/Android/Sdk
```

Android Studio generates this file automatically on first open.

### 3. API keys & environment secrets

PocketPilot ships a fully offline demo experience — the debug build does **not** require any
third-party API keys to run. Networking-related endpoints are declared in
`app/src/main/java/com/example/pocketpilot/core/network/NetworkConfig.kt`.

If you fork the project and want to point it at your own backend, override `NetworkConfig.BASE_URL`
or expose it as a `buildConfigField` (the version catalog already includes what you need to enable
`buildFeatures.buildConfig`).

**Release signing** is driven by environment variables so the same `build.gradle.kts` works locally
and in CI:

| Env var | Purpose |
|---|---|
| `POCKETPILOT_KEYSTORE_PATH` | Absolute path to the `.jks` / `.keystore` |
| `POCKETPILOT_KEYSTORE_PASSWORD` | Keystore password |
| `POCKETPILOT_KEY_ALIAS` | Key alias inside the keystore |
| `POCKETPILOT_KEY_PASSWORD` | Password for that alias |

When any of these are missing (typical for local dev and PRs from forks), the release build type
transparently falls back to the debug signing config so `assembleRelease` still succeeds for smoke
checks.

### 4. Build

```bash
# Debug APK
./gradlew :app:assembleDebug            # → app/build/outputs/apk/debug/app-debug.apk

# Release APK + AAB (requires signing env vars for a fully signed artifact)
./gradlew :app:assembleRelease :app:bundleRelease
```

### 5. Run

Open the project in Android Studio and press **Run**, or install the debug APK on a connected
device:

```bash
./gradlew :app:installDebug
```

---

## Gradle Tasks Cheat Sheet

| Task | What it does |
|---|---|
| `./gradlew :app:assembleDebug` | Build the debug APK |
| `./gradlew :app:assembleRelease` | Build the R8-minified, signed release APK |
| `./gradlew :app:bundleRelease` | Build the Play-ready signed AAB |
| `./gradlew :app:testDebugUnitTest` | Run JVM unit tests (JUnit + MockK + Robolectric) |
| `./gradlew test` | Aggregate unit tests across build types |
| `./gradlew :app:lint` | Android Lint against `config/lint/lint.xml` + baseline |
| `./gradlew detekt` | Detekt static analysis (`config/detekt/detekt.yml` + baseline) |
| `./gradlew ktlintCheck` | KtLint format check across all Kotlin source sets |
| `./gradlew ktlintFormat` | Auto-format Kotlin sources with KtLint |
| `./gradlew clean` | Clean build outputs |

The full validation pass — matching CI — is:

```bash
./gradlew test lint assembleDebug
```

---

## Continuous Integration & Delivery

GitHub Actions ([`.github/workflows/android_ci.yml`](./.github/workflows/android_ci.yml)) runs a
four-stage pipeline on every push and pull request:

1. **`static-analysis`** — `ktlintCheck`, `detekt`, and Android `:app:lint` with HTML/XML/SARIF
   reports uploaded as artifacts.
2. **`unit-tests`** — `./gradlew :app:testDebugUnitTest` with HTML + raw XML reports uploaded.
3. **`build-debug`** — `assembleDebug` on every push/PR; the debug APK is uploaded as an artifact.
4. **`build-release`** — signed `assembleRelease` **and** `bundleRelease` on pushes to `main` and
   `v*` tags only. The keystore is materialised from GitHub Secrets, used to sign the artifacts,
   and wiped before the job exits.

### CI secrets

| Secret | Purpose |
|---|---|
| `RELEASE_KEYSTORE_BASE64` | Base64-encoded release keystore |
| `RELEASE_KEYSTORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Key alias inside the keystore |
| `RELEASE_KEY_PASSWORD` | Password for that key alias |

### CI status badge

The badge at the top of this README (`Android CI`) reflects the latest run of the pipeline on the
`main` branch. It fails the whole workflow if lint, tests, or the debug build fail.

---

## Testing & Quality

PocketPilot ships a layered test pyramid rather than a single flavour of test — each layer
guards its own contract, and each is small enough to run under a second locally.

### Coverage-by-layer

| Layer | Tooling | What is covered |
|---|---|---|
| Validators (pure) | JUnit 4 | Auth email/password rules, transaction & budget invariants |
| Use Cases (domain) | JUnit 4 + MockK | Balance / monthly totals / spend-per-category / add-transaction / savings adjust |
| ViewModels | JUnit 4 + MockK + Turbine + `MainDispatcherRule` | `LoginViewModel`, `DashboardViewModel`, `BudgetFormViewModel` state transitions |
| DAOs | Room in-memory (`androidx.room:room-testing`) + Robolectric | `TransactionDao`, `BudgetDao`, `SavingsGoalDao` reactive queries |
| Design System | Compose UI test + Robolectric | `PocketPilotButton` semantics + click behaviour |
| Formatting | JUnit 4 | `MoneyFormatter` locale-aware output |
| Security | JUnit 4 | `Sha256PinHasher` deterministic salted hashing |

### Test infrastructure

- `testutil/MainDispatcherRule.kt` — swaps `Dispatchers.Main` for a `TestDispatcher` in ViewModel
  tests.
- `testutil/Fixtures.kt` — hand-crafted domain fixtures shared across tests.
- `testutil/TestApplication.kt` — Robolectric application for Compose + Room tests.

### Static analysis (belt-and-braces)

- **KtLint** enforces Android Kotlin style through `.editorconfig` (`ktlint_code_style`).
- **Detekt** with a `config/detekt/detekt.yml` profile — complexity, memory-leak, architectural
  rules — with an `baseline.xml` so new violations fail the build without demanding a
  backlog-clearing PR.
- **Android Lint** with `config/lint/lint.xml` — hard failures on `StopShip`, `HardcodedText`,
  `ContentDescription`, `LabelFor`, and `ClickableViewAccessibility`, and a `lint-baseline.xml`
  for the pre-existing warnings.
- All three analyzers upload reports as CI artifacts for review.

### Running the tests locally

```bash
# Full unit-test suite
./gradlew :app:testDebugUnitTest

# HTML report
open app/build/reports/tests/testDebugUnitTest/index.html
```

---

## Project Roadmap

PocketPilot was built as a **22-phase incremental portfolio project**. This README documents
**Phase 23 — Final Portfolio Polish & Documentation**, which:

- Adds the executive README (this file).
- Adds an MIT `LICENSE`.
- Verifies the whole project with the same commands CI runs: `./gradlew test lint assembleDebug`.

Follow-up work that would layer naturally on top:

- Multi-module Gradle refactor (one module per `feature/*`).
- Migration from manual DI containers to Hilt (aliases already in the version catalog).
- Paging 3 integration for the transactions list (dependencies already tracked).
- Screenshot/UI regression tests via Paparazzi or Roborazzi.
- Play Store production release + Firebase Crashlytics.

---

## License

Released under the [MIT License](./LICENSE). Feel free to fork, learn from, and adapt the code.
If you use it in your own portfolio, an attribution link back is appreciated but not required.
