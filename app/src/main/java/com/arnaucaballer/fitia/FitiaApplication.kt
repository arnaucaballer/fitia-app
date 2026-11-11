package com.arnaucaballer.fitia

import android.app.Application
import com.arnaucaballer.fitia.di.AppContainer

class FitiaApplication : Application() {
    lateinit var container: AppContainer
        private set

    var startupError: Throwable? = null
        private set

    override fun onCreate() {
        super.onCreate()
        try {
            container = AppContainer(this)
        } catch (error: Throwable) {
            startupError = error
        }
    }
}
