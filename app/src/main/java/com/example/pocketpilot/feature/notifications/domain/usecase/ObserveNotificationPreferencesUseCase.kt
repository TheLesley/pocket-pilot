package com.example.pocketpilot.feature.notifications.domain.usecase

import com.example.pocketpilot.feature.notifications.domain.model.NotificationPreferences
import com.example.pocketpilot.feature.notifications.domain.repository.NotificationPreferencesRepository
import kotlinx.coroutines.flow.Flow

class ObserveNotificationPreferencesUseCase(private val repository: NotificationPreferencesRepository) {
    operator fun invoke(): Flow<NotificationPreferences> = repository.observe()
}
