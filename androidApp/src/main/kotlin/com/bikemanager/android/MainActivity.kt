package com.bikemanager.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bikemanager.ui.App
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Napier logging in debug mode
        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog())
        }

        setContent {
            App()
        }
    }
}
