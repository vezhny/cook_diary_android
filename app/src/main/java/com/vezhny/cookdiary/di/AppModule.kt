package com.vezhny.cookdiary.di

import android.content.Context
import com.vezhny.cookdiary.data.AppDatabase
import com.vezhny.cookdiary.data.CookRepository
import com.vezhny.cookdiary.data.SettingsRepository
import com.vezhny.cookdiary.data.TextFiles
import com.vezhny.cookdiary.ui.dishes.DishEditViewModel
import com.vezhny.cookdiary.ui.dishes.DishesViewModel
import com.vezhny.cookdiary.ui.history.HistoryViewModel
import com.vezhny.cookdiary.ui.home.HomeViewModel
import com.vezhny.cookdiary.ui.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.create(androidContext()) }
    single { get<AppDatabase>().dishDao() }
    single { get<AppDatabase>().cookEventDao() }
    single { CookRepository(get(), get()) }
    single { TextFiles(androidContext().contentResolver) }
    single {
        SettingsRepository(androidContext().getSharedPreferences(SettingsRepository.PREFS_NAME, Context.MODE_PRIVATE))
    }

    viewModel { SettingsViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { DishesViewModel(get(), get()) }
    viewModel { params -> DishEditViewModel(params.get(), get()) }
    viewModel { HistoryViewModel(get()) }
}
