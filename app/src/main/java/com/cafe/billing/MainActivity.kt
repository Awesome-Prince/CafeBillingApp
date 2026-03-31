package com.cafe.billing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.cafe.billing.ui.CafeNavGraph
import com.cafe.billing.ui.theme.CafeBillingTheme
import dagger.hilt.android.AndroidEntryPoint

// ============================================================
// MAIN ACTIVITY
// The single Activity for the entire app.
// @AndroidEntryPoint enables Hilt injection in this Activity.
// Everything else is handled by Jetpack Compose + Navigation.
// ============================================================

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw behind system bars for edge-to-edge layout
        enableEdgeToEdge()

        setContent {
            // Respect the system dark/light mode setting
            val isDark = isSystemInDarkTheme()

            CafeBillingTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                CafeNavGraph(navController = navController)
            }
        }
    }
}
