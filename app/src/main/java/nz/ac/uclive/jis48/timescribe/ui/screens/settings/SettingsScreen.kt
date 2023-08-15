package nz.ac.uclive.jis48.timescribe.ui.screens.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import nz.ac.uclive.jis48.timescribe.ui.theme.TimeScribeTheme
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.fragment.app.viewModels
import nz.ac.uclive.jis48.timescribe.R
import nz.ac.uclive.jis48.timescribe.data.Settings
import nz.ac.uclive.jis48.timescribe.models.SettingsViewModel


@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settingsFlow.collectAsState(initial = Settings())
    val workDurationState = remember { mutableStateOf(settings.workDuration) }
    val breakDurationState = remember { mutableStateOf(settings.breakDuration) }
    val longBreakDurationState = remember { mutableStateOf(settings.longBreakDuration) }
    val cyclesBeforeLongBreakState = remember { mutableStateOf(settings.cyclesBeforeLongBreak) }
    val soundNotificationState = remember { mutableStateOf(settings.soundNotification) }
    val popupNotificationState = remember { mutableStateOf(settings.popupNotification) }
    val darkModeState = remember { mutableStateOf(settings.darkMode) }

    Button(onClick = {
        val newSettings = Settings(
            workDuration = workDurationState.value,
            breakDuration = breakDurationState.value,
            longBreakDuration = longBreakDurationState.value,
            cyclesBeforeLongBreak = cyclesBeforeLongBreakState.value,
            soundNotification = soundNotificationState.value,
            popupNotification = popupNotificationState.value,
            darkMode = darkModeState.value
        )
        viewModel.saveSettings(newSettings)
    }) {
        Text("Save")
    }


    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = stringResource(R.string.pomodoro_settings_label), style = MaterialTheme.typography.h6)
        PomodoroWorkDuration(workDurationState)
        Text(text = stringResource(R.string.notification_settings_label), style = MaterialTheme.typography.h6)
        NotificationSettings(soundNotificationState, popupNotificationState)
        DarkModeSetting(darkModeState)
    }

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PomodoroWorkDuration(workDurationState: MutableState<Int>) {
    val expanded = remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val customValue = remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = stringResource(R.string.work_duration_label))
        Box {
            Text(
                text = if (workDurationState.value == -1) stringResource(R.string.custom_label) else workDurationState.value.toString(),
                modifier = Modifier.clickable { expanded.value = true }
            )
            DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                val commonDurations = listOf(20, 25, 50, 55, 120)
                commonDurations.forEach { duration ->
                    DropdownMenuItem(onClick = {
                        workDurationState.value = duration
                        expanded.value = false
                    }) {
                        Text(text = duration.toString())
                    }
                }
                DropdownMenuItem(onClick = {
                    workDurationState.value = -1
                    expanded.value = false
                }) {
                    Text(text = stringResource(R.string.custom_label))
                }
            }
        }
    }
    if (workDurationState.value == -1) {
        TextField(
            value = customValue.value,
            onValueChange = { customValue.value = it },
            label = { Text(text = stringResource(R.string.custom_value_label)) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    workDurationState.value = customValue.value.toIntOrNull() ?: 25
                    customValue.value = ""
                }
            )
        )
    }
}

@Composable
fun NotificationSettings(
    soundNotificationState: MutableState<Boolean>,
    popupNotificationState: MutableState<Boolean>
) {
    ToggleSetting(label = stringResource(R.string.sound_notification_label), checkedState = soundNotificationState)
    ToggleSetting(label = stringResource(R.string.popup_notification_label), checkedState = popupNotificationState)
}

@Composable
fun ToggleSetting(label: String, checkedState: MutableState<Boolean>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Switch(checked = checkedState.value, onCheckedChange = { checkedState.value = it })
    }
}

@Composable
fun DarkModeSetting(darkModeState: MutableState<Boolean>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Dark Mode")
        Switch(checked = darkModeState.value, onCheckedChange = { darkModeState.value = it })
    }
}


class SettingsFragment : Fragment() {
    private val viewModel: SettingsViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TimeScribeTheme {
                    SettingsScreen(viewModel)
                }
            }
        }
    }
}
