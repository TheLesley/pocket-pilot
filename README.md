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
- **Security:** Biometric app lock using AndroidX Biometric and KeyStore APIs.
- **Adaptive UI:** Supports compact phones, foldables, and tablets via `WindowSizeClass`.

---

## 🎨 Design System

PocketPilot features a "Dark Fintech" aesthetic:
- **Background / Surfaces:** Dark Slate (`#0F1117` base / `#191C24` cards)
- **Brand Primary:** Indigo (`#6366F1`)
- **Semantic Indicators:** Mint Green (`#34D399`) for income, Coral Red (`#FB7185`) for expenses, Cyan (`#22D3EE`) for transfers

---

## 🏗️ Tech Stack & Architecture

Built with **Clean Architecture** and **Unidirectional Data Flow (UDF)**.

- **UI:** Jetpack Compose, Material Design 3, Navigation Compose
- **Architecture & DI:** Hilt, ViewModel, Coroutines, Flow, StateFlow
- **Data & Storage:** Room (SQLite), DataStore Preferences, WorkManager (background sync)
- **Quality Gates:** JUnit 5, MockK, Turbine, Detekt, Ktlint, Android Lint

### Project Structure

```text
app/
 ├── presentation/          # Compose screens, ViewModels, UI state, navigation
 │    ├── dashboard/
 │    ├── transactions/     # List, filters, forms
 │    ├── analytics/        # Category charts
 │    └── theme/            # Theme tokens (Color.kt, Theme.kt)
 ├── domain/                # Pure Kotlin use cases and models
 │    ├── model/            # Transaction, Budget, Category
 │    ├── repository/       # Repository contracts
 │    └── usecase/          # Encapsulated domain logic
 ├── data/                  # Data layer
 │    ├── local/            # Room database, DAOs, DataStore
 │    ├── remote/           # Retrofit service interfaces & DTOs
 │    └── repository/       # Offline-first repository implementations
 └── di/                    # Hilt modules
