package com.vezhny.cookdiary.di

import com.vezhny.cookdiary.data.AppDatabase
import com.vezhny.cookdiary.data.CookRepository
import com.vezhny.cookdiary.domain.DishPicker
import com.vezhny.cookdiary.ui.dishes.DishEditViewModel
import com.vezhny.cookdiary.ui.dishes.DishesViewModel
import com.vezhny.cookdiary.ui.history.HistoryViewModel
import com.vezhny.cookdiary.ui.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.create(androidContext()) }
    single { get<AppDatabase>().dishDao() }
    single { get<AppDatabase>().cookEventDao() }
    single { CookRepository(get(), get()) }
    factory { DishPicker() }

    viewModel { HomeViewModel(get(), get()) }
    viewModel { DishesViewModel(get()) }
    viewModel { params -> DishEditViewModel(params.get(), get()) }
    viewModel { HistoryViewModel(get()) }
}
