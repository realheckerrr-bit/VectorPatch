package dev.vectorpatch.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import dev.vectorpatch.ui.navigation.VectorPatchNavGraph
import dev.vectorpatch.ui.theme.ThemeMode
import dev.vectorpatch.ui.theme.VectorPatchTheme
import dev.vectorpatch.viewmodel.SettingsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT  -> false
                ThemeMode.DARK   -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            VectorPatchTheme(darkTheme = darkTheme) {
                VectorPatchNavGraph()
            }
        }
    }
}
