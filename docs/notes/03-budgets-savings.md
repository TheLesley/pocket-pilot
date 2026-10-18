# 03 – Budgets & savings goals

Files: `feature/finance/domain/model/Budget.kt`, `BudgetProgress.kt`, `SavingsGoal.kt`, `domain/usecase/CalculateBudgetSpentUseCase.kt`, `ObserveBudgetsProgressUseCase.kt`, `AdjustSavingsContributionUseCase.kt`, `presentation/budget/`, `presentation/savings/`

> ⚠️ These screens exist (`BudgetNavHost`, `SavingsNavHost`) but nothing in the app navigates to them yet. See [09](09-known-issues.md).

## Budgets
`Budget` = name, limit (minor units), currency, `period` (`WEEKLY / MONTHLY / YEARLY / CUSTOM`), start date, optional end date, optional `categoryId`.

**How "spent" is worked out** (`CalculateBudgetSpentUseCase`): add up every **expense** in the same currency, between the budget's start and end date, that either has `budgetId == budget.id` **or** matches the budget's category. (The transaction form always sets `budgetId = null`, so in practice the category match is what links them.)

**Progress** (`BudgetProgress`): `ratio = spent / limit`, `percent`, `remaining`, and a status: `ON_TRACK` below 80 %, `WARNING` from 80 %, `EXCEEDED` at 100 %+. These are computed properties (`get()`), so they're always consistent with the numbers.

**Live updates** (`ObserveBudgetsProgressUseCase`): `combine(budgets flow, transactions flow)` – whenever either table changes, progress is recalculated.

## Savings goals
`SavingsGoal` = name, target, amount saved so far, optional target date, note.

`AdjustSavingsContributionUseCase(id, delta)`:
- delta can be positive (add money) or negative (withdraw)
- never goes below zero (`coerceAtLeast(0)`)
- returns a sealed result: `Applied(newSaved)`, `NotFound`, or `NoChange` – the caller must handle all three. That's cleaner than throwing exceptions for normal outcomes.

## Concepts to own
- **Computed properties** on data classes keep derived values in one place.
- **`combine`** – merges the latest values of several Flows; emits when any of them emits.
- **Sealed result types** for expected outcomes instead of exceptions.

## Questions
1. How does a transaction get counted against a budget?
<details><summary>Show answer</summary>It must be an expense, same currency, within the budget's dates, and either linked by budgetId or share the budget's category.</details>

2. What does `combine` do in `ObserveBudgetsProgressUseCase`?
<details><summary>Show answer</summary>Takes the latest budgets list and latest transactions list and recalculates progress whenever either changes.</details>

3. Why return `AdjustSavingsContributionUseCase.Result` instead of throwing?
<details><summary>Show answer</summary>"Not found" and "no change" are normal outcomes, not errors; a sealed type forces the caller to handle each one and is easy to test.</details>

4. What happens if a limit is 0?
<details><summary>Show answer</summary>`ratio` is 1 if anything was spent, else 0 – avoids dividing by zero.</details>

5. A monthly budget with no end date – does "spent" reset each month?
<details><summary>Show answer</summary>No. It sums everything since the start date. That's a bug to fix: spent should be calculated within the current period window (see 09).</details>

6. Where would you add a "you've hit 80 %" notification?
<details><summary>Show answer</summary>It already exists: `EvaluateBudgetAlertsUseCase` in the notifications feature reads budget progress and creates warning/exceeded alerts (note 07).</details>
