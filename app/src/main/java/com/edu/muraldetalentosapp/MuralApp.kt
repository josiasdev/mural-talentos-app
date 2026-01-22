package com.edu.muraldetalentosapp

import android.app.Application
import com.edu.muraldetalentosapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MuralApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MuralApp)
            modules(appModule)
        }
    }
}