package app.fitia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.fitia.R
import app.fitia.design.FitiaSpace
import app.fitia.design.FitiaTheme
import app.fitia.navigation.FitiaApp

@Composable
private fun StartupError(error: Throwable) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(FitiaSpace.page()),
        verticalArrangement = Arrangement.spacedBy(FitiaSpace.stack),
    ) {
        Text(text = stringResource(R.string.startup_failed))
        Text(text = error.message ?: error.javaClass.simpleName)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitiaTheme {
                val startupError = (application as FitiaApplication).startupError
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (startupError != null) {
                        StartupError(startupError)
                    } else {
                        FitiaApp()
                    }
                }
            }
        }
    }
}
