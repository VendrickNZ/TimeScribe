package nz.ac.uclive.jis48.timescribe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import nz.ac.uclive.jis48.timescribe.data.ScreenItem
import nz.ac.uclive.jis48.timescribe.models.TimerViewModel
import nz.ac.uclive.jis48.timescribe.ui.screens.timer.TimerScreen
import nz.ac.uclive.jis48.timescribe.ui.screens.history.HistoryScreen
import nz.ac.uclive.jis48.timescribe.ui.screens.settings.SettingsScreen
import nz.ac.uclive.jis48.timescribe.ui.theme.TimeScribeTheme

const val TIMER_ROUTE = "Timer"
const val HISTORY_ROUTE = "History"
const val SETTINGS_ROUTE = "Settings"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: TimerViewModel = ViewModelProvider(this)[TimerViewModel::class.java]

        setContent {
            TimeScribeTheme {
                MainScreen(viewModel)
            }
        }
    }

    @Composable
    fun MainScreen(viewModel: TimerViewModel) {

        val navController = rememberNavController()
        val items = listOf(
            ScreenItem(TIMER_ROUTE, R.string.timer_button, R.drawable.timer_icon),
            ScreenItem(HISTORY_ROUTE, R.string.history_button, R.drawable.history_icon),
            ScreenItem(SETTINGS_ROUTE, R.string.settings_button, R.drawable.settings_icon)
        )



        Scaffold(
            bottomBar = {
                BottomNavigation {
                    val navBackStackEntry = navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry.value?.destination?.route
                    items.forEach { screenItem ->
                        BottomNavigationItem(
                            icon = {
                                Icon(
                                    painterResource(id = screenItem.iconResId),
                                    contentDescription = "Navbar Icon"
                                )
                            },
                            label = { Text(stringResource(id = screenItem.labelResId)) },
                            selected = currentRoute == screenItem.route,
                            onClick = {
                                if (currentRoute != screenItem.route) {
                                    navController.navigate(screenItem.route)
                                }
                            }
                        )
                    }
                }
            }
        )
        { paddingValues ->
            NavHost(navController, startDestination = "Timer") {
                composable("Timer") {
                    TimerScreen(paddingValues, viewModel)
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
