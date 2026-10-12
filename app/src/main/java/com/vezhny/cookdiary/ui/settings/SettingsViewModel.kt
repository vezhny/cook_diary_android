package com.vezhny.cookdiary.ui.settings

import androidx.lifecycle.ViewModel
import com.vezhny.cookdiary.data.AppLanguage
import com.vezhny.cookdiary.data.SettingsRepository
import com.vezhny.cookdiary.data.ThemeMode
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(private val settings: SettingsRepository) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = settings.themeMode

    fun setThemeMode(mode: ThemeMode) = settings.setThemeMode(mode)

    // Read on demand: changing the language recreates the activity, while this ViewModel survives.
    fun language(): AppLanguage = AppLanguage.current()

    fun setLanguage(language: AppLanguage) {
        if (language != AppLanguage.current()) AppLanguage.apply(language)
    }
}
