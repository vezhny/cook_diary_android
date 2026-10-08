package com.vezhny.cookdiary

import android.app.Application
import com.vezhny.cookdiary.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CookDiaryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CookDiaryApplication)
            modules(appModule)
        }
    }
}
