package com.example.pocketpilot.feature.notifications.domain.usecase

import com.example.pocketpilot.feature.finance.domain.model.BudgetProgress
import com.example.pocketpilot.feature.notifications.domain.model.AlertCategory
import com.example.pocketpilot.feature.notifications.domain.model.AlertNotification
import com.example.pocketpilot.feature.notifications.domain.model.NotificationChannelType
import com.example.pocketpilot.feature.notifications.domain.model.NotificationPreferences
import kotlin.math.abs

/**
 * Pure evaluation: given the current [BudgetProgress] list and the user's
 * alert preferences, decide which [AlertNotification]s should fire *now*.
 *
 * A budget produces at most one alert per pass:
 *   - "Exceeded" (ratio >= 1) beats "warning" (ratio >= configured threshold).
 *   - Budgets below the threshold produce nothing.
 *
 * Notification ids are derived deterministically from the budget id so a
 * subsequent evaluation replaces the previous alert instead of stacking a new
 * one every worker tick — that behaviour also lets the "exceeded" alert
 * overwrite an earlier "warning" cleanly.
 */
class EvaluateBudgetAlertsUseCase {

    operator fun invoke(progresses: List<BudgetProgress>, preferences: NotificationPreferences): List<AlertNotification> {
        if (!preferences.masterEnabled || !preferences.budgetAlertsEnabled) return emptyList()

        val warningRatio = preferences.warningRatio
        val out = mutableListOf<AlertNotification>()
        for (progress in progresses) {
            val ratio = progress.ratio
            val exceeded = ratio >= 1f
            val warning = !exceeded && ratio >= warningRatio && warningRatio > 0f
            if (!exceeded && !warning) continue

            val budget = progress.budget
            val id = notificationIdFor(budget.id)
            val notification = if (exceeded) {
                AlertNotification(
                    id = id,
                    channel = NotificationChannelType.BUDGET_ALERTS,
                    title = "Budget exceeded: ${budget.name}",
                    body = "You've spent ${progress.percent}% of your ${budget.name} budget.",
                    category = AlertCategory.BUDGET_EXCEEDED
                )
            } else {
                AlertNotification(
                    id = id,
                    channel = NotificationChannelType.BUDGET_ALERTS,
                    title = "Budget warning: ${budget.name}",
                    body = "You're at ${progress.percent}% of your ${budget.name} budget.",
                    category = AlertCategory.BUDGET_WARNING
                )
            }
            out += notification
        }
        return out
    }

    /** Stable, non-negative Int derived from a budget id. */
    private fun notificationIdFor(budgetId: String): Int = NOTIFICATION_ID_BUDGET_OFFSET + (abs(budgetId.hashCode()) % 100_000)

    companion object {
        const val NOTIFICATION_ID_BUDGET_OFFSET: Int = 10_000
    }
}
