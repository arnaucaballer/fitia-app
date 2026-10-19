package com.arnaucaballer.fitia

import android.app.Application
import com.arnaucaballer.fitia.di.AppContainer

class FitiaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
