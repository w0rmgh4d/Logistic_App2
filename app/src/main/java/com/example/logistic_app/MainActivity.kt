package com.example.logistic_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.logistic_app.ui.MainScreen
import com.example.logistic_app.ui.theme.Logistic_AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Initialize OSMDroid configuration before setting content
        Configuration.getInstance().userAgentValue = packageName
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        // This variable controls when the splash screen is dismissed
        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        // Launch a coroutine to wait for 2 seconds (2000ms)
        lifecycleScope.launch {
            delay(2000)
            keepSplashScreen = false
        }

        enableEdgeToEdge()
        setContent {
            Logistic_AppTheme {
                MainScreen()
            }
        }
    }
}
