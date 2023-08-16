package nz.ac.uclive.jis48.timescribe

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import nz.ac.uclive.jis48.timescribe.data.*
import nz.ac.uclive.jis48.timescribe.models.HistoryViewModel
import nz.ac.uclive.jis48.timescribe.models.SettingsViewModel
import nz.ac.uclive.jis48.timescribe.models.SettingsViewModelFactory
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
        createNotificationChannel()


        // I apologize to all who hath eyes
        val settingsRepository = SettingsRepository(context = this)
        val settingsViewModel: SettingsViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(settingsRepository)
        )[SettingsViewModel::class.java]

        val timerRepository = TimerRepository(context = this)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TimerViewModel(settingsViewModel, timerRepository = timerRepository) as T
            }
        }
        val timerViewModel: TimerViewModel = ViewModelProvider(this, factory)[TimerViewModel::class.java]

        val historyViewModel: HistoryViewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HistoryViewModel(timerRepository, timerViewModel) as T
                }
            }
        )[HistoryViewModel::class.java]

        timerViewModel.timeIsOverEvent.observe(this) { isOver ->
            if (isOver) {
                val intent = Intent(this, TimerService::class.java).apply {
                    action = ACTION_NOTIFY_TIME_IS_OVER
                }
                ContextCompat.startForegroundService(this, intent)
                timerViewModel.timeIsOverEvent.value = false
            }
        }


        setContent {
            val settings by settingsViewModel.settingsFlow.collectAsState(initial = Settings())
            TimeScribeTheme (darkModeState = settings.darkMode) {
                MainScreen(timerViewModel, settingsViewModel, historyViewModel)
            }
        }
    }

    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    @Composable
    fun MainScreen(timerViewModel: TimerViewModel, settingsViewModel: SettingsViewModel,
                   historyViewModel: HistoryViewModel) {

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
                composable(TIMER_ROUTE) {
                    val settings by settingsViewModel.settingsFlow.collectAsState(initial = Settings())
                    TimerScreen(paddingValues, timerViewModel, settings.darkMode)
                }
                composable(HISTORY_ROUTE) {
                    HistoryScreen(paddingValues, historyViewModel)
                }
                composable(SETTINGS_ROUTE) {
                    SettingsScreen(settingsViewModel)
                }
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "timescribe_channel"
    }
}
