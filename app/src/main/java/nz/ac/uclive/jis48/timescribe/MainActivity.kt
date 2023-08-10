package nz.ac.uclive.jis48.timescribe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import nz.ac.uclive.jis48.timescribe.data.ScreenItem
import nz.ac.uclive.jis48.timescribe.ui.screens.timer.TimerScreen
import nz.ac.uclive.jis48.timescribe.ui.screens.history.HistoryScreen
import nz.ac.uclive.jis48.timescribe.ui.screens.settings.SettingsScreen
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
            ScreenItem("Timer", R.drawable.timer_icon),
            ScreenItem("History", R.drawable.history_icon),
            ScreenItem("Settings", R.drawable.settings_icon)

        )

        Scaffold(
            bottomBar = {
                BottomNavigation {
                    val navBackStackEntry = navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry.value?.destination?.route
                    items.forEach { screenItem ->
                        BottomNavigationItem(
                            icon = { Icon(painterResource(id = screenItem.iconResId), contentDescription = "Navbar Icon") },
                            label = { Text(screenItem.name) },
                            selected = currentRoute == screenItem.name,
                            onClick = {
                                if (currentRoute != screenItem.name) {
                                    navController.navigate(screenItem.name)
                                }
                            }
                        )
                    }
                }
            }

        ) { paddingValues ->
            NavHost(navController, startDestination = "Timer") {
                composable("Timer") {
                    TimerScreen(paddingValues)
                }
                composable("History") {
                    HistoryScreen(paddingValues)
                }
                composable("Settings") {
                    SettingsScreen(paddingValues)
                }
            }
        }
    }
}
