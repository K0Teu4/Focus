package com.example.focusflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusflow.data.repository.SettingsRepository
import com.example.focusflow.services.LicenseValidator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PremiumViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)

    /** true только если флаг стоит И срок не истёк (0 = бессрочно) */
    val isPremium: StateFlow<Boolean> = combine(
        repository.isPremium,
        repository.premiumExpiresAt
    ) { active, expiresAt ->
        active && (expiresAt == 0L || expiresAt > System.currentTimeMillis())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** 0 = бессрочно, иначе timestamp окончания */
    val premiumExpiresAt: StateFlow<Long> = repository.premiumExpiresAt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun activateCode(code: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = LicenseValidator.validate(code)
            if (ok) {
                repository.saveIsPremium(true)
                repository.savePremiumCode(code.trim().uppercase())
                repository.savePremiumExpiresAt(0L) // коды — бессрочно
            }
            onResult(ok)
        }
    }

    fun restorePurchase(onResult: (Boolean) -> Unit) {
        // Заглушка до RuStore Pay SDK: премиум хранится локально
        viewModelScope.launch {
            onResult(isPremium.value)
        }
    }
}