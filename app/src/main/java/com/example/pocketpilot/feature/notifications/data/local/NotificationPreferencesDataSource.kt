package com.example.pocketpilot.feature.notifications.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.pocketpilot.feature.notifications.data.mapper.toDomain
import com.example.pocketpilot.feature.notifications.domain.model.NotificationPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * DataStore wrapper for alert/notification preferences. Isolated from the
 * settings feature's own store so a "clear preferences" reset there cannot
 * silently disable alerts the user still wants to receive.
 *
 * IO errors while reading are downgraded to defaults so a corrupt file does
 * not crash the worker on the first background pass after boot.
 */
class NotificationPreferencesDataSource(private val dataStore: DataStore<Preferences>) {

    val preferencesFlow: Flow<NotificationPreferences> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { it.toDomain() }

    suspend fun get(): NotificationPreferences = preferencesFlow.first()

    suspend fun setMasterEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.MasterEnabled] = enabled }
    }

    suspend fun setBudgetAlertsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.BudgetAlertsEnabled] = enabled }
    }

    suspend fun setBudgetWarningThresholdPercent(percent: Int) {
        dataStore.edit { it[Keys.BudgetWarningThresholdPercent] = percent.coerceIn(0, 100) }
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DailyReminderEnabled] = enabled }
    }

    suspend fun setDailyReminderTime(hourOfDay: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.DailyReminderHour] = hourOfDay.coerceIn(0, 23)
            prefs[Keys.DailyReminderMinute] = minute.coerceIn(0, 59)
        }
    }

    internal object Keys {
        val MasterEnabled = booleanPreferencesKey("notifications_master_enabled")
        val BudgetAlertsEnabled = booleanPreferencesKey("notifications_budget_alerts_enabled")
        val BudgetWarningThresholdPercent = intPreferencesKey("notifications_budget_warning_percent")
        val DailyReminderEnabled = booleanPreferencesKey("notifications_daily_reminder_enabled")
        val DailyReminderHour = intPreferencesKey("notifications_daily_reminder_hour")
        val DailyReminderMinute = intPreferencesKey("notifications_daily_reminder_minute")
    }

    companion object {
        private const val DATASTORE_NAME = "pocketpilot_notification_preferences"

        private val Context.notificationDataStore by preferencesDataStore(name = DATASTORE_NAME)

        fun create(context: Context): NotificationPreferencesDataSource =
            NotificationPreferencesDataSource(context.applicationContext.notificationDataStore)
    }
}
