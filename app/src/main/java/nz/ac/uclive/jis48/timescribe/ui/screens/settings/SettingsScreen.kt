package nz.ac.uclive.jis48.timescribe.ui.screens.settings

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kotlinx.coroutines.delay
import nz.ac.uclive.jis48.timescribe.R
import nz.ac.uclive.jis48.timescribe.data.Settings
import nz.ac.uclive.jis48.timescribe.models.SettingsViewModel
import nz.ac.uclive.jis48.timescribe.ui.components.CustomKeyboardOverlay
import nz.ac.uclive.jis48.timescribe.ui.theme.TimeScribeTheme

enum class SettingType {
    WorkDuration,
    BreakDuration,
    LongBreakDuration,
    CyclesBeforeLongBreak
}

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

    val animationDurationMillis = 300
    val currentSetting = remember { mutableStateOf<SettingType?>(null) }

    if (!showCustomKeyboard.value && customValue.value.isNotEmpty()) {
        LaunchedEffect(showCustomKeyboard.value) {
            delay(animationDurationMillis.toLong())
            customValue.value = ""
        }
    }

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
                currentSetting = currentSetting
            )
            PomodoroBreakDuration(
                breakDuration = settings.breakDuration,
                onUpdateBreakDuration = updateBreakDuration,
                showCustomKeyboard = showCustomKeyboard,
                currentSetting = currentSetting
            )
            PomodoroLongBreakDuration(
                longBreakDuration = settings.longBreakDuration,
                onUpdateLongBreakDuration = updateLongBreakDuration,
                showCustomKeyboard = showCustomKeyboard,
                currentSetting = currentSetting
            )
            PomodoroCyclesBeforeLongBreak(
                cyclesBeforeLongBreak = settings.cyclesBeforeLongBreak,
                onUpdateCyclesBeforeLongBreak = updateCyclesBeforeLongBreak,
                showCustomKeyboard = showCustomKeyboard,
                currentSetting = currentSetting
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.other_label),
                style = MaterialTheme.typography.h6,
                modifier = if (isLandscape) Modifier.align(Alignment.CenterHorizontally) else Modifier
            )
            DarkModeSetting(darkModeState = settings.darkMode, onUpdateDarkMode = updateDarkMode)
        }
        CustomKeyboardOverlay(
            visible = showCustomKeyboard.value,
            inputText = customValue.value,
            onKeyPress = { key -> customValue.value += key },
            onDeletePress = {
                if (customValue.value.isNotEmpty()) {
                    customValue.value = customValue.value.dropLast(1)
                }
            },
            onConfirm = {
                val newValue = customValue.value.toIntOrNull() ?: 0
                when (currentSetting.value) {
                    SettingType.WorkDuration -> updateWorkDuration(newValue)
                    SettingType.BreakDuration -> updateBreakDuration(newValue)
                    SettingType.LongBreakDuration -> updateLongBreakDuration(newValue)
                    SettingType.CyclesBeforeLongBreak -> updateCyclesBeforeLongBreak(newValue)
                    else -> {}
                }
                customValue.value = ""
                showCustomKeyboard.value = false
                currentSetting.value = null
            },
            onDismiss = {
                showCustomKeyboard.value = false
                currentSetting.value = null
            },
            bottomPadding = paddingValues
        )

        if (!showCustomKeyboard.value && customValue.value.isNotEmpty()) {
            LaunchedEffect(showCustomKeyboard.value) {
                delay(animationDurationMillis.toLong())
                customValue.value = ""
            }
        }

    }
}

@Composable
fun PomodoroWorkDuration(
    workDuration: Int,
    onUpdateWorkDuration: (Int) -> Unit,
    showCustomKeyboard: MutableState<Boolean>,
    currentSetting: MutableState<SettingType?>
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
                    currentSetting.value = SettingType.WorkDuration
                    Log.d("SettingsScreen", "checking currentSetting: ${currentSetting.value}")
                    showCustomKeyboard.value = true
                }) {
                    Text(text = stringResource(R.string.custom_label))
                }
            }
        }
    }
}

@Composable
fun PomodoroBreakDuration(
    breakDuration: Int,
    onUpdateBreakDuration: (Int) -> Unit,
    showCustomKeyboard: MutableState<Boolean>,
    currentSetting: MutableState<SettingType?>
) {
    val expanded = remember { mutableStateOf(false) }

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
                    expanded.value = false
                    currentSetting.value = SettingType.BreakDuration
                    showCustomKeyboard.value = true
                }) {
                    Text(text = stringResource(R.string.custom_label))
                }
            }
        }
    }
}

@Composable
fun PomodoroLongBreakDuration(
    longBreakDuration: Int,
    onUpdateLongBreakDuration: (Int) -> Unit,
    showCustomKeyboard: MutableState<Boolean>,
    currentSetting: MutableState<SettingType?>
) {
    val expanded = remember { mutableStateOf(false) }

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
                    expanded.value = false
                    currentSetting.value = SettingType.LongBreakDuration
                    showCustomKeyboard.value = true
                }) {
                    Text(text = stringResource(R.string.custom_label))
                }
            }
        }
    }
}

@Composable
fun PomodoroCyclesBeforeLongBreak(
    cyclesBeforeLongBreak: Int,
    onUpdateCyclesBeforeLongBreak: (Int) -> Unit,
    showCustomKeyboard: MutableState<Boolean>,
    currentSetting: MutableState<SettingType?>
) {
    val expanded = remember { mutableStateOf(false) }

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
                val commonDurations = listOf(2, 3, 4)
                commonDurations.forEach { duration ->
                    DropdownMenuItem(onClick = {
                        onUpdateCyclesBeforeLongBreak(duration)
                        expanded.value = false
                    }) {
                        Text(text = duration.toString())
                    }
                }
                DropdownMenuItem(onClick = {
                    expanded.value = false
                    currentSetting.value = SettingType.CyclesBeforeLongBreak
                    showCustomKeyboard.value = true
                }) {
                    Text(text = stringResource(R.string.custom_label))
                }
            }
        }
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
