package nz.ac.uclive.jis48.timescribe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import nz.ac.uclive.jis48.timescribe.ui.theme.TimeScribeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimeScribeTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {
        val navController = rememberNavController()
        val items = listOf(
            "Timer", "History", "Settings"
        )

        Scaffold(
            bottomBar = {
                BottomNavigation {
                    val navBackStackEntry = navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry.value?.destination?.route
                    items.forEach { screen ->
                        BottomNavigationItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = null) }, // Placeholder icon for now
                            label = { Text(screen) },
                            selected = currentRoute == screen,
                            onClick = {
                                if (currentRoute != screen) {
                                    navController.navigate(screen)
                                }
                            }
                        )
                    }
                }
            }
        ) { _ ->
            NavHost(navController, startDestination = "Timer") {
                composable("Timer") { /* Show TimerFragment's Composables here */ }
                composable("History") { /* Show HistoryFragment's Composables here */ }
                composable("Settings") { /* Show SettingsFragment's Composables here */ }
            }
        }
    }
}

