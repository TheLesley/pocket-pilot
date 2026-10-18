# 04 – Dashboard, analytics & adaptive layout

Files: `feature/dashboard/domain/usecase/*`, `presentation/DashboardViewModel.kt`, `DashboardScreen.kt`, `DashboardNavHost.kt`; `feature/analytics/...`; `core/designsystem/component/AdaptiveNavScaffold.kt`; `MainActivity.kt`

## Dashboard
`ObserveDashboardSummaryUseCase` = `combine(transactions, budgets)` → builds one `DashboardSummary`:
- balance (`CalculateBalanceUseCase`: income − expenses, all time)
- this month's income & expenses (`CalculateMonthlyTotalsUseCase` with a `MonthRange`)
- monthly budget total (`CalculateMonthlyBudgetUseCase`: weekly × 52/12, yearly ÷ 12, custom prorated)
- spending by category, and the 5 most recent transactions

Each calculation is its own tiny **pure** use case (input → output, no side effects). That's why they're the easiest things to test – look at the `CalculateXxxUseCaseTest` files.

`clock` is injected so tests can pin "today". Currency is taken from the first transaction (or "USD") – mixed currencies aren't handled (see 09).

## Navigation (hand-written, no Navigation Compose)
`DashboardNavHost.kt` defines:
- `sealed interface HomeDestination` – `Dashboard`, `TransactionList`, `TransactionDetail(id)`, `TransactionForm(mode, prefillType)`
- `HomeNavController` – holds the current destination in a `MutableState`; `navigateTo`, `selectTab`
- `HomeNavHost` – `AnimatedContent` + `when (destination)` picks which screen to show, with slide/fade transitions.

The auth flow does the same with an `enum AuthDestination` and `rememberSaveable`. Trade-off: simple and type-safe, but no back stack handling, deep links or saved state across process death that Navigation Compose gives you.

## Adaptive layout
`MainActivity` calls `calculateWindowSizeClass(activity)` and `AdaptiveNavScaffold` picks:
- **Compact** (phones) → bottom `NavigationBar`
- **Medium** (foldables, small tablets) → side `NavigationRail`
- **Expanded** (tablets) → a permanent side panel

## Analytics (built, not yet reachable)
`ObserveAnalyticsReportUseCase` → for a date filter (this month, this year, custom…) builds cash-flow summary, category distribution and a trend series (monthly points for year views).
Charts are drawn by hand with Compose **`Canvas`**: `DonutChart` loops over slices and calls `drawArc` with `sweep = value / total × 360°`; also `GroupedBarChart`, `TrendLineChart`. No chart library.

## Questions
1. Why split the dashboard maths into several small use cases?
<details><summary>Show answer</summary>Each is pure and single-purpose, so it's easy to test and reuse (analytics and notifications reuse similar logic).</details>

2. How is the navigation implemented, and what would Navigation Compose add?
<details><summary>Show answer</summary>A sealed `HomeDestination` held in Compose state and switched with `when` inside `AnimatedContent`. Navigation Compose adds a back stack, deep links, saved state and standard argument passing.</details>

3. What's `WindowSizeClass` for?
<details><summary>Show answer</summary>Choosing a layout by available width: bottom bar on phones, rail on medium, permanent drawer on large screens.</details>

4. How does the donut chart work?
<details><summary>Show answer</summary>A Compose Canvas draws one arc per category; each arc's sweep angle is its share of the total × 360°, starting where the previous one ended.</details>

5. Why inject `clock` into the summary use case?
<details><summary>Show answer</summary>So tests can fix the current date and get predictable month ranges.</details>

6. How would you convert weekly and yearly budgets to a monthly figure?
<details><summary>Show answer</summary>Weekly × 52/12, yearly ÷ 12, custom prorated by how much of its span a month covers – as in `CalculateMonthlyBudgetUseCase`.</details>
