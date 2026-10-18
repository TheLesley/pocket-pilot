# 08 – Testing & CI

Files: `app/src/test/**`, `.github/workflows/android_ci.yml`, `config/` (detekt rules)

## What's tested (90 `@Test` functions, JUnit 4)
| Area | Files | Tool |
|---|---|---|
| Validators | TransactionValidator (10), AuthValidator (8), BudgetValidator (6) | plain JUnit |
| Pure calculators | CalculateBalance / MonthlyTotals / MonthlyBudget / SpendingByCategory / BudgetSpent, AdjustSavingsContribution | plain JUnit |
| Use cases with repositories | LoginUseCase, AddTransactionUseCase, ObserveTransactionsUseCase | MockK, `runTest` |
| ViewModels | LoginViewModel, DashboardViewModel, BudgetFormViewModel | MockK, Turbine, `MainDispatcherRule` |
| Room DAOs | TransactionDao, BudgetDao, SavingsGoalDao | Robolectric + in-memory Room |
| Compose UI | LoginScreen, PocketPilotButton | Compose UI test (`createComposeRule`) on Robolectric |
| Other | MoneyFormatter, Sha256PinHasher | plain JUnit |

Not tested yet: sync engines/SyncManager, workers, AppLockManager, most screens, the transaction list/form ViewModels.

## The tools
- **`runTest`** – runs coroutine tests and skips `delay`s so they're instant.
- **`MainDispatcherRule`** – ViewModels launch on `Dispatchers.Main`, which doesn't exist on the JVM. The rule swaps it for a test dispatcher before each test and resets after.
- **MockK** – fakes dependencies: `coEvery { loginUseCase(email, pw) } returns user`; `coVerify(exactly = 0) { loginUseCase(any(), any()) }` proves invalid input never reaches the use case.
- **Turbine** – tests Flows: `viewModel.effect.test { ...; assertEquals(LoginEffect.NavigateToHome, awaitItem()) }`.
- **Robolectric** – runs Android code (Room, Compose) on the JVM, no emulator. DAO tests use `Room.inMemoryDatabaseBuilder(...).allowMainThreadQueries()` so each test gets a fresh, throwaway database.
- Test names in backticks read like sentences: `` `submit with invalid email surfaces validation error without calling use case` ``.

## CI pipeline (`.github/workflows/android_ci.yml`)
Runs on every push/PR to `main` (and tags):
1. **static-analysis** – Android Lint, detekt (code smells: long methods, complexity), ktlint (formatting)
2. **unit-tests** – `./gradlew test`, uploads the HTML report
3. **build-debug** – `assembleDebug`
4. **build-release** – release APK/AAB on `main` and tags; signs with a keystore from GitHub Secrets if present, otherwise builds unsigned (your latest commit added that fallback)

`concurrency` cancels an older run when you push again. Your git history shows you fixing detekt/ktlint failures – that's exactly what CI is for.

## Questions
1. Why do ViewModel tests need `MainDispatcherRule`?
<details><summary>Show answer</summary>`viewModelScope` uses Dispatchers.Main, which isn't available in JVM tests; the rule replaces it with a test dispatcher.</details>

2. What's Turbine for?
<details><summary>Show answer</summary>Collecting a Flow in a test and asserting each emitted item in order (`awaitItem()`), with clear failures on timeouts or unexpected items.</details>

3. Why use an in-memory Room database in DAO tests?
<details><summary>Show answer</summary>Each test starts clean, nothing is written to disk, and it's fast.</details>

4. Mock vs fake – what's the difference?
<details><summary>Show answer</summary>A mock (MockK) is configured per test and can verify calls; a fake is a simple working implementation (like `FakeAuthRemoteDataSource`). Fakes are often more readable for repositories.</details>

5. What do detekt and ktlint catch that the compiler doesn't?
<details><summary>Show answer</summary>ktlint: formatting/style. detekt: code smells – overly long or complex functions, naming, potential bugs.</details>

6. What would you test next?
<details><summary>Show answer</summary>The sync engines with a fake remote (insert/update/delete, retries, conflict strategies), AppLockManager timing, and the transaction list ViewModel (filters, retry).</details>
