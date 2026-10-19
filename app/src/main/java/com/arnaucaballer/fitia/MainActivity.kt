package com.arnaucaballer.fitia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.arnaucaballer.fitia.design.FitiaTheme
import com.arnaucaballer.fitia.navigation.FitiaApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitiaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FitiaApp()
                }
            }
        }
    }
}
