# 02 – Transactions

Files: `feature/finance/` → `domain/model/Transaction.kt`, `data/local/entity/TransactionEntity.kt`, `data/local/dao/TransactionDao.kt`, `data/local/PocketPilotDatabase.kt`, `data/local/query/TransactionQuerySqlBuilder.kt`, `data/repository/TransactionRepositoryImpl.kt`, `domain/usecase/SearchTransactionsUseCase.kt`, `domain/validation/TransactionValidator.kt`, `presentation/list|form|detail/`

## The model, two versions
- `Transaction` (domain) – what the app thinks about: title, `amountMinorUnits: Long`, currency, `INCOME/EXPENSE`, category, date, note.
- `TransactionEntity` (data) – the Room table `transactions`. Same fields **plus** sync bookkeeping: `sync_status`, `local_updated_at`, `last_sync_error`. Mappers (`FinanceMappers.kt`) convert between them, so sync details never leak into the UI.
- IDs are `UUID` strings created on the phone – needed for offline-first, because you can't wait for a server to hand out IDs.

## Room details worth knowing
- **Indices** on `occurred_at`, `category_id`, `sync_status`, `amount_minor_units`, `budget_id` – these are the columns the app sorts/filters by, so SQLite doesn't scan the whole table.
- **Foreign key** to `budgets` with `onDelete = SET_NULL`: deleting a budget doesn't delete its transactions, it just unlinks them.
- **Migrations** 1→2→3→4 in `PocketPilotDatabase`: add sync columns, add indices, add the `auth_accounts` table. Migrations upgrade a user's existing database without wiping data. `exportSchema = false` means schema history isn't saved to JSON (would be better turned on).
- DAO queries return `Flow<List<...>>` → Room re-emits whenever the table changes.
- Every read query has `WHERE sync_status != 'PENDING_DELETE'` – deleted-but-not-yet-synced rows are hidden.

## Writes are offline-first (`TransactionRepositoryImpl`)
- `upsert`: new row → `PENDING_INSERT`; editing a row that was never uploaded stays `PENDING_INSERT` (so the server gets a create, not an update of something it never saw); otherwise `PENDING_UPDATE`.
- `delete`: if the row was never uploaded, delete it for real. Otherwise mark it `PENDING_DELETE` – a **tombstone** – so sync can tell the server, then remove it.
- A `clock: () -> Long` is injected instead of calling `System.currentTimeMillis()` directly, so tests can control time.

## Search, filter, sort
1. `TransactionListViewModel` keeps a `MutableStateFlow<TransactionQuery>`. Every event (typing, chip toggle, sort) updates state **and** pushes a new query.
2. `SearchTransactionsUseCase`:
   - `debounce` – waits 250 ms after typing stops (0 ms if the search box is empty) so Room isn't queried on every key press.
   - `distinctUntilChanged` – ignores identical queries.
   - `flatMapLatest` – cancels the previous database subscription and starts a new one for the new query.
3. `TransactionQuerySqlBuilder` builds the SQL. User values are **bound with `?` placeholders**, never pasted into the string – that prevents SQL injection. `%` and `_` in the search term are escaped so they're treated literally.
4. `@RawQuery(observedEntities = [TransactionEntity::class])` – lets Room run a SQL string built at runtime and still give back a live `Flow`.

## Form & validation
- `TransactionValidator` returns a sealed `TransactionFieldError` per field (or null), so each field can show its own message.
- Amount text like "12,50" or "12.50" → `MoneyFormatter.parseToMinorUnits` → `1250`.
- On save the ViewModel calls `AddTransactionUseCase` or `EditTransactionUseCase`, then sends a `Saved` effect so the screen navigates back.

## Questions
1. Why have both `Transaction` and `TransactionEntity`?
<details><summary>Show answer</summary>Separation of concerns: the entity carries database and sync details; the domain model is what the rest of the app uses. Changing the database doesn't ripple into the UI.</details>

2. Explain `debounce` + `flatMapLatest` in the search.
<details><summary>Show answer</summary>Debounce waits until typing pauses before emitting; flatMapLatest switches to a new Room query and cancels the old one, so only the latest search's results are shown.</details>

3. How does the query builder prevent SQL injection?
<details><summary>Show answer</summary>User input is never concatenated into the SQL; it's passed as bound arguments to `?` placeholders. Only fixed column names and sort clauses are concatenated.</details>

4. What's a tombstone and why use one?
<details><summary>Show answer</summary>A row marked `PENDING_DELETE` instead of deleted, so the sync engine still knows to tell the server. It's hidden from the UI and removed after the server confirms.</details>

5. Why generate IDs with UUID on the device?
<details><summary>Show answer</summary>Offline-first: records must have a unique ID before the server ever sees them.</details>

6. What is a Room migration and what happens without one?
<details><summary>Show answer</summary>Code that upgrades an existing database schema to a new version. Without it Room throws on upgrade (or wipes data if destructive migration is allowed).</details>
