# PocketPilot — Native Android Guidelines

## Project Goal
Production-quality Native Android app for a Junior/Graduate Android Engineer portfolio.
Must follow official Android modern architecture standards, clean code, and zero "tutorial tier" anti-patterns.

## Tech Stack
- **Language & UI:** Kotlin, Jetpack Compose, Material 3
- **Architecture:** MVVM + Clean Architecture + Unidirectional Data Flow (UDF)
- **Async & Streams:** Kotlin Coroutines, Flow / StateFlow
- **DI & Persistence:** Hilt, Room, DataStore
- **Networking & Async Jobs:** Retrofit, WorkManager, Paging 3
- **Build System:** Gradle Kotlin DSL (`.gradle.kts`) with Version Catalogs (`libs.versions.toml`)

## Architecture Principles
UI (Compose) ➔ ViewModel ➔ Use Case ➔ Repository ➔ Data Sources (Room / Retrofit)
- Primary source of truth: Local Room Database (Offline-first approach).
- Absolute separation: Separate DTOs, Local Room Entities, and Domain Models with Explicit Mappers.
- No direct Retrofit/Room calls from UI or ViewModel.
- UI state must strictly use `StateFlow`.

## Incremental Workflow Rules
1. Build incrementally phase by phase.
2. NEVER generate the entire app at once.
3. Before writing code:
    - State what will be built.
    - List files/modules to create or modify.
4. After writing code:
    - Run Gradle compile/test check (`./gradlew assembleDebug test`).
    - Fix all compilation/test errors before declaring a phase complete.
5. Provide 3–5 Junior Android Interview Questions related to the active feature at the end of each phase.