package nz.ac.uclive.jis48.timescribe.ui.screens.timer

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import nz.ac.uclive.jis48.timescribe.R
import nz.ac.uclive.jis48.timescribe.data.Settings
import nz.ac.uclive.jis48.timescribe.data.TimerService
import nz.ac.uclive.jis48.timescribe.models.SettingsViewModel
import nz.ac.uclive.jis48.timescribe.models.TimerViewModel
import nz.ac.uclive.jis48.timescribe.ui.theme.*

@Composable
fun TimerScreen(paddingValues: PaddingValues, viewModel: TimerViewModel, darkMode: Boolean) {
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val showResetDialog = remember { mutableStateOf(false) }
    val showConfirmTransitionDialog = remember { mutableStateOf(false) }
    val currentStateDuration = viewModel.getCurrentStateDuration()
    val workDuration = viewModel.getCurrentWorkDuration()
    val progress = viewModel.getProgress()

    val textColor = when (viewModel.timerState.value) {
        TimerViewModel.TimerState.WORK -> if (darkMode) lightRed else darkRed
        TimerViewModel.TimerState.BREAK -> if (darkMode) lightBlue else darkBlue
        TimerViewModel.TimerState.LONG_BREAK -> if (darkMode) darkerLightBlue else darkBlue
        else -> MaterialTheme.colors.onBackground
    }

    if (showDialog.value) {
        AlertDialog(
            title = { Text(text = stringResource(R.string.stop_timer_label)) },
            text = { Text(text = stringResource(R.string.are_you_sure_stop_timer_label)) },
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                Button(onClick = {
                    viewModel.stopTimer(context)
                    showDialog.value = false
                }) {
                    Text(text = stringResource(R.string.yes_label))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text(text = stringResource(R.string.no_label))
                }
            }
        )
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (workDuration != 0) {
                Text(
                    text = currentStateDuration,
                    style = MaterialTheme.typography.h6,
                    color = textColor
                )
                LinearProgressIndicator(
                    progress = progress,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))
            Text(
                text = viewModel.getFormattedTime(),
                style = MaterialTheme.typography.h1
            )
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                when (viewModel.timerState.value) {
                    TimerViewModel.TimerState.WAITING_FOR_USER -> {
                        Button(onClick = { viewModel.continueToNextState() }) {
                            Text(text = "Click to Continue")
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = { showResetDialog.value = true }) {
                            Text(text = stringResource(R.string.reset_button))
                        }
                        if (showResetDialog.value) {
                            ConfirmActionDialog(
                                title = stringResource(R.string.reset_timer_label),
                                message = stringResource(R.string.are_you_sure_reset_timer_label),
                                onConfirm = { viewModel.resetTimer(context) },
                                onDismiss = { showResetDialog.value = false }
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = { showDialog.value = true }) {
                            Text(text = stringResource(R.string.stop_button))
                        }
                    }
                    TimerViewModel.TimerState.IDLE -> {
                        if (viewModel.timeElapsedState == 0) {
                            Button(onClick = { viewModel.startTimer(context) }) {
                                Text(text = stringResource(R.string.start_button))
                            }
                        } else {
                            Button(
                                onClick = { viewModel.resumeTimer(context) },
                                modifier = Modifier.width(125.dp)
                            ) {
                                Text(text = stringResource(R.string.resume_button))
                            }
                            Spacer(modifier = Modifier.width(8.dp)) // Adding a space between buttons

                            Button(onClick = { showResetDialog.value = true }) {
                                Text(text = stringResource(R.string.reset_button))
                            }
                            if (showResetDialog.value) {
                                ConfirmActionDialog(
                                    title = stringResource(R.string.reset_timer_label),
                                    message = stringResource(R.string.are_you_sure_reset_timer_label),
                                    onConfirm = { viewModel.resetTimer(context) },
                                    onDismiss = { showResetDialog.value = false }
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp)) // Adding a space between buttons

                            Button(onClick = { showDialog.value = true }) {
                                Text(text = stringResource(R.string.stop_button))
                            }
                        }
                    }

                    TimerViewModel.TimerState.WORK, TimerViewModel.TimerState.BREAK, TimerViewModel.TimerState.LONG_BREAK -> {
                        Button(onClick = { viewModel.pauseTimer(context) }) {
                            Text(text = stringResource(R.string.pause_button))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1.5f))
        }
    }
}

@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = { Text(text = title) },
        text = { Text(text = message) },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(text = stringResource(R.string.yes_label))
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(text = stringResource(R.string.no_label))
            }
        }
    )
}


class TimerFragment : Fragment() {
    private val timerViewModel: TimerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels() // Assuming you have a ViewModel for settings

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val settings by settingsViewModel.settingsFlow.collectAsState(initial = Settings())
                val darkMode = settings.darkMode
                TimeScribeTheme {
                    TimerScreen(PaddingValues(0.dp), timerViewModel, darkMode)
                }
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timerViewModel.timeIsOverEvent.observe(viewLifecycleOwner) { isTimeOver ->
            if (isTimeOver) {
                timerViewModel.notifyTimeIsOver(requireContext())
                timerViewModel.timeIsOverHandled()
            }
        }
    }
}
