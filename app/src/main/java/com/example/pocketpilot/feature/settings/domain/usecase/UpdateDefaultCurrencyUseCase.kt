package com.example.pocketpilot.feature.settings.domain.usecase

import com.example.pocketpilot.feature.settings.domain.repository.SettingsRepository
import java.util.Currency

class UpdateDefaultCurrencyUseCase(private val repository: SettingsRepository) {
    /**
     * @throws IllegalArgumentException if [currencyCode] is not a valid ISO 4217 code.
     */
    suspend operator fun invoke(currencyCode: String) {
        val normalised = currencyCode.trim().uppercase()
        // `Currency.getInstance` throws IllegalArgumentException for unknown codes —
        // let it propagate so the caller can surface a validation error rather
        // than persisting bad data.
        Currency.getInstance(normalised)
        repository.setDefaultCurrencyCode(normalised)
    }
}
