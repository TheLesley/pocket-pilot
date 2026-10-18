# 09 – Known issues & what I'd fix

Read this before every interview. Being able to name your own app's weaknesses – and how you'd fix them – impresses interviewers far more than pretending it's perfect.

## Fix before applying (an interviewer who installs the app will notice)
1. **Most features aren't reachable.** `HomeNavHost` only has Dashboard and Transactions. `BudgetNavHost`, `SavingsNavHost`, `AnalyticsNavHost`, `SettingsNavHost` and `NotificationSettingsScreen` are never called. Because Settings is unreachable, the PIN/biometric lock can't be switched on either.
   *Fix:* add `Budgets`, `Savings`, `Analytics`, `Settings` to `HomeTab` / `HomeDestination`, add `AdaptiveNavItem`s in `MainActivity`, and add `when` branches in `HomeNavHost`. A good first task to do **yourself**.
2. **You have to log in on every app start.** `authenticated` in `MainActivity` uses plain `remember`, and the token is in `InMemoryAuthTokenStore`. *Fix:* store the session in DataStore and read it at start-up (`ObserveAuthSessionUseCase` exists).
3. **The old README was wrong** (Hilt, KeyStore, JUnit 5, folder layout). Fixed on 1 Oct 2026 – keep it matching the code.

## Real bugs / weaknesses to know about
4. **Sync runs against a server that doesn't exist.** Every 15 minutes and on every reconnect the engines call `api.pocketpilot.app`, fail (treated as "no connectivity", so retried) and write `last_sync_error` on every pending row. *Fix:* skip sync when `NetworkConfig.hasRemoteBackend` is false.
5. **Budgets don't reset each period.** `CalculateBudgetSpentUseCase` sums everything from the budget's start date, so a "monthly" budget with no end date keeps growing forever. *Fix:* calculate spent within the current week/month/year window.
6. **Budget alerts can repeat.** The alert worker runs every 15 min and re-posts any budget over the threshold. Same ID means it updates rather than stacks, but it can buzz again. *Fix:* remember which alerts were already sent this period.
7. **Mixed currencies are added together.** Balance and dashboard totals ignore currency; the dashboard just uses the first transaction's currency (or "USD"). *Fix:* group by currency or convert.
8. **Retry after an error does nothing.** In `TransactionListViewModel`, `.catch {}` ends the Flow, so the Retry event updates `queryFlow` but nothing is collecting any more. *Fix:* re-launch the collection on Retry, or use `retryWhen`.
9. **Editing a transaction resets its created date.** The form sets `createdAtEpochMillis = now` for edits too. *Fix:* keep the original value.
10. **Weak hashing.** Passwords and PINs use one round of salted SHA-256. Fast hashes + small PIN space = brute-forceable if the database is copied. *Fix:* PBKDF2/bcrypt/Argon2 and/or Android Keystore, plus attempt limits.
11. **Password reset has no email step** – the code is generated and stored locally.
12. **`exportSchema = false`** on the Room database – no schema history to test migrations against. *Fix:* export schemas and add `MigrationTestHelper` tests.
13. **No DI framework** – manual containers. Fine for learning; Hilt would remove the factories and container boilerplate.

## Good "what would you do next?" answers
- Wire up the remaining screens and persist the session (items 1–2).
- Build a small backend (e.g. Ktor or Firebase) so sync is real.
- Migrate DI to Hilt.
- Add tests for sync engines and AppLockManager.
- Swap hand-rolled navigation for type-safe Navigation Compose.
