package com.jprogrammingpro.aiscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.jprogrammingpro.aiscanner.ui.navigation.AiScannerRoot
import com.jprogrammingpro.aiscanner.ui.theme.AiScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as AiScannerApp).container
        setContent {
            AiScannerTheme {
                AiScannerRoot(container)
            }
        }
    }
}
