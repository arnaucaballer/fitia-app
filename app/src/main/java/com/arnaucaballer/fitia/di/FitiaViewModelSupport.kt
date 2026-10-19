package com.arnaucaballer.fitia.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arnaucaballer.fitia.FitiaApplication

fun Context.fitiaContainer(): AppContainer =
    (applicationContext as FitiaApplication).container

fun <T : ViewModel> fitiaViewModelFactory(
    container: AppContainer,
    create: (AppContainer) -> T,
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
            return create(container) as VM
        }
    }
}
