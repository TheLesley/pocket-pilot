# 07 – Notifications & settings

Files: `feature/notifications/*`, `feature/settings/*`, `feature/security/data/local/SecurityPreferencesDataSource.kt`

> ⚠️ Settings and Notification Settings screens exist but aren't reachable from navigation yet (09). The background workers **do** run.

## DataStore Preferences
Small settings (theme, default currency, lock options, notification options) are stored with **DataStore**, not Room and not SharedPreferences.
- `dataStore.data` is a `Flow<Preferences>` → mapped to a domain model (`UserPreferences`, `NotificationPreferences`).
- `.catch { if (it is IOException) emit(emptyPreferences()) else throw it }` – if the file is unreadable, fall back to defaults instead of crashing.
- Writes use `dataStore.edit { it[Keys.ThemeMode] = mode.name }` – suspend, atomic, off the main thread.
- Why not SharedPreferences? It does disk I/O on the main thread and has no Flow/transaction support.

## Notifications
**Budget alerts** – `BudgetAlertWorker`, every 15 min (WorkManager's minimum):
1. read preferences; stop if notifications or budget alerts are off
2. take the first emission of `ObserveBudgetsProgressUseCase` (`.first()`)
3. `EvaluateBudgetAlertsUseCase` (pure function) → list of alerts: "warning" at the threshold (default 80 %), "exceeded" at 100 %
4. `AndroidNotificationDispatcher.post(...)`

Each budget gets a **stable notification ID** (`10_000 + hash(budgetId)`), so a newer alert replaces the old one rather than stacking.

**Daily reminder** – `DailyReminderWorker`, a one-time job with `setInitialDelay` until the chosen time (default 20:00), `ExistingWorkPolicy.REPLACE` so changing the time cancels the old one. It's rescheduled at app start-up.

**Dispatcher safety** – before posting it checks the Android 13+ `POST_NOTIFICATIONS` permission, that notifications are enabled, and that the channel isn't muted. `NotificationChannels` creates the channels (required since Android 8).

**`NotificationWorkerFactory`** + `DelegatingWorkerFactory` – one factory for sync workers and one for notification workers, combined so WorkManager can build both.

## Questions
1. Why DataStore over SharedPreferences?
<details><summary>Show answer</summary>DataStore is async (coroutines/Flow), safe from main-thread I/O, handles errors and gives atomic updates; SharedPreferences can block the UI thread.</details>

2. Why does every budget have a fixed notification ID?
<details><summary>Show answer</summary>Posting with the same ID updates the existing notification instead of creating a new one each run.</details>

3. Why one-time work with an initial delay for the daily reminder, not periodic work?
<details><summary>Show answer</summary>Periodic work drifts and can't target an exact time of day; a one-time job delayed until the chosen time is more precise.</details>

4. What must you check before posting a notification on Android 13+?
<details><summary>Show answer</summary>The runtime POST_NOTIFICATIONS permission; also that notifications/the channel aren't disabled.</details>

5. Why is `EvaluateBudgetAlertsUseCase` a pure function?
<details><summary>Show answer</summary>Given progress + preferences it returns alerts with no side effects, so it can be unit-tested without Android; the worker handles the side effects.</details>

6. What's a `DelegatingWorkerFactory`?
<details><summary>Show answer</summary>A WorkerFactory that asks a list of factories in turn, so different worker families with different dependencies can be created by one WorkManager configuration.</details>
