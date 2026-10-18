# 01 – Architecture & the ViewModel pattern

Files: `core/ui/base/BaseViewModel.kt`, `core/ui/base/UiContract.kt`, `core/ui/UiState.kt`, `core/common/Result.kt`, any `feature/*/di/*Container.kt`

## Clean Architecture in one picture
```
Screen (Compose)  ──Event──▶  ViewModel  ──calls──▶  UseCase  ──calls──▶  Repository (interface, domain)
      ▲                          │                                              │
      └──── State (StateFlow) ◀──┘                         RepositoryImpl (data) → Room DAO / Retrofit API
      └──── Effect (SharedFlow, one-off: navigate, show error)
```
Rule: arrows only point **inwards**. `domain/` knows nothing about Room, Retrofit or Android. That's why use cases and validators can be tested with plain JUnit, no emulator.

## BaseViewModel: State, Event, Effect
Every ViewModel extends `BaseViewModel<State, Event, Effect>`:
- **State** – one immutable data class describing the whole screen (e.g. `TransactionListState`). Held in a `MutableStateFlow`, exposed read-only as `state: StateFlow`. Changed only through `setState { copy(...) }`, which uses `_state.update(reducer)` – atomic, so two coroutines can't overwrite each other.
- **Event** – everything the user can do, as a sealed interface (`SearchChanged`, `AddClicked`…). The screen calls `viewModel.onEvent(event)`; the ViewModel's `handleEvent` uses a `when` over the sealed type, so the compiler forces you to handle every event.
- **Effect** – one-time things (navigate, show snackbar). Sent through a `MutableSharedFlow` with `replay = 0`, so rotating the screen doesn't navigate twice. `extraBufferCapacity = 1, DROP_OLDEST` means `emit` never blocks.

This pattern is often called **MVI** (Model-View-Intent) or "unidirectional data flow": data flows down as State, user actions flow up as Events.

The "Contract" files (`LoginContract.kt`, `TransactionListContract.kt`…) just hold that screen's State, Event and Effect types together.

## UiState and Result
- `Result<T>` (core/common) = `Success(data) | Error(throwable) | Loading`. Used by data/domain code. Helpers: `map`, `onSuccess`, `onError`, `asResult()` turns any `Flow<T>` into `Flow<Result<T>>` (Loading first, errors caught).
- `UiState<T>` (core/ui) = `Idle | Loading | Empty | Success | Error(message)`. What screens render. `Result.toUiState()` converts, and can turn an empty list into `Empty` so the UI shows an empty-state message.
- Both are `sealed`, so `when` must cover every case. `@Immutable` tells Compose these never change after creation, so it can skip recomposing.

## Dependency injection – manual "Containers", not Hilt
There's no DI library. Each feature has an `object XxxContainer` (a **service locator**) that builds the graph:
```
FinanceContainer: Room DB → DAOs → repositories   (database created once, thread-safe double-checked locking)
TransactionContainer: repositories → use cases (by lazy)
TransactionListViewModelFactory: use cases → ViewModel
```
- `by lazy` = create on first use, then reuse (a singleton).
- `@Volatile` + `synchronized` in `init` = **double-checked locking**, so two threads can't create two databases.
- `ViewModelProvider.Factory` classes exist because a ViewModel with constructor parameters can't be created by Android without one.
- Comments say "a Hilt module will replace this in a later phase". With Hilt you'd replace containers with `@Module`/`@Provides`, annotate ViewModels with `@HiltViewModel @Inject constructor(...)`, and delete the factories.

**How to say it in an interview:** "I wired dependency injection manually with containers and ViewModel factories so I understood exactly what a DI framework does. The graph is shaped like Hilt modules, so migrating is mechanical."

## Questions
1. What's the difference between `StateFlow` and `SharedFlow`, and why does the app use one for State and the other for Effects?
<details><summary>Show answer</summary>`StateFlow` always holds a current value and replays it to new collectors – perfect for screen state. `SharedFlow` with replay 0 doesn't keep a value, so one-off events like "navigate" fire once and aren't repeated on rotation.</details>

2. Why is the domain layer free of Android imports?
<details><summary>Show answer</summary>So business logic can be unit-tested quickly on the JVM and doesn't depend on frameworks; data sources can be swapped without touching it.</details>

3. What does `_state.update { }` give you over `_state.value = ...`?
<details><summary>Show answer</summary>It's atomic (compare-and-set), so concurrent updates don't lose changes.</details>

4. What is a service locator and how is it different from Hilt?
<details><summary>Show answer</summary>An object you ask for dependencies (`TransactionContainer.addTransactionUseCase`). Hilt instead pushes dependencies into constructors at compile time and manages scopes; less boilerplate, compile-time checks.</details>

5. Why do ViewModels here need a `ViewModelProvider.Factory`?
<details><summary>Show answer</summary>Android creates ViewModels and only knows how to call a no-argument constructor; the factory tells it how to pass use cases (and IDs) in.</details>

6. Why make Event a sealed interface?
<details><summary>Show answer</summary>`when` over a sealed type is exhaustive – add a new event and the compiler makes you handle it.</details>
