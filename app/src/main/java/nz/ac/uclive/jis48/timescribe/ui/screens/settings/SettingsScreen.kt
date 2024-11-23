package nz.ac.uclive.jis48.timescribe.ui.screens.settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import nz.ac.uclive.jis48.timescribe.R
import nz.ac.uclive.jis48.timescribe.data.Settings
import nz.ac.uclive.jis48.timescribe.models.SettingsViewModel
import nz.ac.uclive.jis48.timescribe.ui.components.CustomKeyboardOverlay
import nz.ac.uclive.jis48.timescribe.ui.theme.TimeScribeTheme


@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val settings by viewModel.settingsFlow.collectAsState(initial = Settings())
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val showCustomKeyboard = remember { mutableStateOf(false) }
    val customValue = remember { mutableStateOf("") }

    val updateWorkDuration: (Int) -> Unit = { newDuration ->
        val newSettings = settings.copy(workDuration = newDuration)
        viewModel.saveSettings(newSettings)
    }

    val updateBreakDuration: (Int) -> Unit = { newDuration ->
        val newSettings = settings.copy(breakDuration = newDuration)
        viewModel.saveSettings(newSettings)
    }

    val updateLongBreakDuration: (Int) -> Unit = { newDuration ->
        val newSettings = settings.copy(longBreakDuration = newDuration)
        viewModel.saveSettings(newSettings)
    }

    val updateCyclesBeforeLongBreak: (Int) -> Unit = { newCycles ->
        val newSettings = settings.copy(cyclesBeforeLongBreak = newCycles)
        viewModel.saveSettings(newSettings)
    }

    val updateDarkMode: (Boolean) -> Unit = { newSetting ->
        val newSettings = settings.copy(darkMode = newSetting)
        viewModel.saveSettings(newSettings)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .let { if (isLandscape) it.padding(horizontal = 60.dp) else it }
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.pomodoro_settings_label),
                style = MaterialTheme.typography.h6,
                modifier = if (isLandscape) Modifier.align(Alignment.CenterHorizontally) else Modifier
            )
            PomodoroWorkDuration(
                workDuration = settings.workDuration,
                onUpdateWorkDuration = updateWorkDuration,
                showCustomKeyboard = showCustomKeyboard,
            )
            PomodoroBreakDuration(
                breakDuration = settings.breakDuration,
                onUpdateBreakDuration = updateBreakDuration
            )
            PomodoroLongBreakDuration(
                longBreakDuration = settings.longBreakDuration,
                onUpdateLongBreakDuration = updateLongBreakDuration
            )
            PomodoroCyclesBeforeLongBreak(
                cyclesBeforeLongBreak = settings.cyclesBeforeLongBreak,
                onUpdateCyclesBeforeLongBreak = updateCyclesBeforeLongBreak
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.other_label),
                style = MaterialTheme.typography.h6,
                modifier = if (isLandscape) Modifier.align(Alignment.CenterHorizontally) else Modifier
            )
            DarkModeSetting(darkModeState = settings.darkMode, onUpdateDarkMode = updateDarkMode)
        }
        if (showCustomKeyboard.value) {
            CustomKeyboardOverlay(
                inputText = customValue.value,
                onKeyPress = { key -> customValue.value += key },
                onDeletePress = {
                    if (customValue.value.isNotEmpty()) {
                        customValue.value = customValue.value.dropLast(1)
                    }
                },
                onConfirm = {
                    val newDuration = customValue.value.toIntOrNull() ?: 25
                    updateWorkDuration(newDuration)
                    customValue.value = ""
                    showCustomKeyboard.value = false
                },
                onDismiss = {
                    customValue.value = ""
                    showCustomKeyboard.value = false
                },
                bottomPadding = paddingValues
            )
        }
    }
}

@Composable
fun PomodoroWorkDuration(
    workDuration: Int,
    onUpdateWorkDuration: (Int) -> Unit,
    showCustomKeyboard: MutableState<Boolean>,
) {
    val expanded = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = stringResource(R.string.work_duration_label))
        Box {
            Text(
                text = if (workDuration == -1) stringResource(R.string.custom_label) else workDuration.toString(),
                modifier = Modifier.clickable { expanded.value = true }
            )
            DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                val commonDurations = listOf(0, 20, 25, 50, 55, 120)
                commonDurations.forEach { duration ->
                    DropdownMenuItem(onClick = {
                        onUpdateWorkDuration(duration)
                        expanded.value = false
                    }) {
                        Text(text = duration.toString())
                    }
                }
                DropdownMenuItem(onClick = {
                    expanded.value = false
                    showCustomKeyboard.value = true
                }) {
                    Text(text = stringResource(R.string.custom_label))
                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PomodoroBreakDuration(breakDuration: Int, onUpdateBreakDuration: (Int) -> Unit) {
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
        Text(text = stringResource(R.string.break_duration_label))
        Box {
            Text(
                text = if (breakDuration == -1) stringResource(R.string.custom_label) else breakDuration.toString(),
                modifier = Modifier.clickable { expanded.value = true }
            )
            DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                val commonDurations = listOf(5, 10, 15, 20, 30)
                commonDurations.forEach { duration ->
                    DropdownMenuItem(onClick = {
                        onUpdateBreakDuration(duration)
                        expanded.value = false
                    }) {
                        Text(text = duration.toString())
                    }
                }
                DropdownMenuItem(onClick = {
                    onUpdateBreakDuration(-1)
                    expanded.value = false
                }) {
                    Text(text = stringResource(R.string.custom_label))
                }
            }
        }
    }
    if (breakDuration == -1) {
        TextField(
            value = customValue.value,
            onValueChange = { customValue.value = it },
            label = { Text(text = stringResource(R.string.custom_value_label)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    val newDuration = customValue.value.toIntOrNull() ?: 5
                    onUpdateBreakDuration(newDuration)
                    customValue.value = ""
                }
            )
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PomodoroLongBreakDuration(longBreakDuration: Int, onUpdateLongBreakDuration: (Int) -> Unit) {
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
        Text(text = stringResource(R.string.long_break_duration_label))
        Box {
            Text(
                text = if (longBreakDuration == -1) stringResource(R.string.custom_label) else longBreakDuration.toString(),
                modifier = Modifier.clickable { expanded.value = true }
            )
            DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                val commonDurations = listOf(30, 60, 90, 120)
                commonDurations.forEach { duration ->
                    DropdownMenuItem(onClick = {
                        onUpdateLongBreakDuration(duration)
                        expanded.value = false
                    }) {
                        Text(text = duration.toString())
                    }
                }
                DropdownMenuItem(onClick = {
                    onUpdateLongBreakDuration(-1)
                    expanded.value = false
                }) {
                    Text(text = stringResource(R.string.custom_label))
                }
            }
        }
    }
    if (longBreakDuration == -1) {
        TextField(
            value = customValue.value,
            onValueChange = { customValue.value = it },
            label = { Text(text = stringResource(R.string.custom_value_label)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    val newDuration = customValue.value.toIntOrNull() ?: 15
                    onUpdateLongBreakDuration(newDuration)
                    customValue.value = ""
                }
            )
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PomodoroCyclesBeforeLongBreak(
    cyclesBeforeLongBreak: Int,
    onUpdateCyclesBeforeLongBreak: (Int) -> Unit
) {
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
        Text(text = stringResource(R.string.cycles_before_long_break_label))
        Box {
            Text(
                text = if (cyclesBeforeLongBreak == -1) stringResource(R.string.custom_label) else cyclesBeforeLongBreak.toString(),
                modifier = Modifier.clickable { expanded.value = true }
            )
            DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                val commonCycles = listOf(2, 3, 4)
                commonCycles.forEach { cycles ->
                    DropdownMenuItem(onClick = {
                        onUpdateCyclesBeforeLongBreak(cycles)
                        expanded.value = false
                    }) {
                        Text(text = cycles.toString())
                    }
                }
                DropdownMenuItem(onClick = {
                    onUpdateCyclesBeforeLongBreak(-1)
                    expanded.value = false
                }) {
                    Text(text = stringResource(R.string.custom_label))
                }
            }
        }
    }
    if (cyclesBeforeLongBreak == -1) {
        TextField(
            value = customValue.value,
            onValueChange = { customValue.value = it },
            label = { Text(text = stringResource(R.string.custom_value_label)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    val newDuration = customValue.value.toIntOrNull() ?: 4
                    onUpdateCyclesBeforeLongBreak(newDuration)
                    customValue.value = ""
                }
            )
        )
    }
}

@Composable
fun DarkModeSetting(darkModeState: Boolean, onUpdateDarkMode: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = stringResource(R.string.dark_mode_label))
        Switch(
            checked = darkModeState,
            onCheckedChange = { onUpdateDarkMode(it) },
            modifier = Modifier.size(20.dp)
        )
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
                val settings by viewModel.settingsFlow.collectAsState(initial = Settings())
                val darkMode = settings.darkMode
                TimeScribeTheme(darkMode) {
                    SettingsScreen(viewModel)
                }
            }
        }
    }
}
