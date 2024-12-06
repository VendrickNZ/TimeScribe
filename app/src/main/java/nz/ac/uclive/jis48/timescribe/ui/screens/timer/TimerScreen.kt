package nz.ac.uclive.jis48.timescribe.ui.screens.timer

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import nz.ac.uclive.jis48.timescribe.R
import nz.ac.uclive.jis48.timescribe.data.Settings
import nz.ac.uclive.jis48.timescribe.models.SettingsViewModel
import nz.ac.uclive.jis48.timescribe.models.TimerViewModel
import nz.ac.uclive.jis48.timescribe.ui.theme.BreakColorDark
import nz.ac.uclive.jis48.timescribe.ui.theme.BreakColorLight
import nz.ac.uclive.jis48.timescribe.ui.theme.LongBreakColorDark
import nz.ac.uclive.jis48.timescribe.ui.theme.LongBreakColorLight
import nz.ac.uclive.jis48.timescribe.ui.theme.TimeScribeTheme
import nz.ac.uclive.jis48.timescribe.ui.theme.WorkColorDark
import nz.ac.uclive.jis48.timescribe.ui.theme.WorkColorLight

@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    val context = LocalContext.current
    val showStopDialog = remember { mutableStateOf(false) }
    val showResetDialog = remember { mutableStateOf(false) }
    val currentStateDuration = viewModel.getCurrentStateDuration()
    val workDuration = viewModel.getCurrentWorkDuration()
    val progress = viewModel.getProgress()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val isLightTheme = MaterialTheme.colors.isLight

    val timerStateColor = when (viewModel.lastNonIdleStateForColour) {
        TimerViewModel.TimerState.WORK -> if (isLightTheme) WorkColorLight else WorkColorDark
        TimerViewModel.TimerState.BREAK -> if (isLightTheme) BreakColorLight else BreakColorDark
        TimerViewModel.TimerState.LONG_BREAK -> if (isLightTheme) LongBreakColorLight else LongBreakColorDark
        else -> MaterialTheme.colors.onBackground
    }

    val buttonTextColour = if (isLightTheme) Color.Black else Color.White

    if (showStopDialog.value) {
        ConfirmActionDialog(
            title = stringResource(R.string.stop_timer_label),
            message = stringResource(R.string.are_you_sure_stop_timer_label),
            timerStateColor = timerStateColor,
            buttonTextColour = buttonTextColour,
            onConfirm = {
                viewModel.stopTimer(context)
                showStopDialog.value = false
                Toast.makeText(
                    context,
                    context.getString(R.string.session_saved_toast_label),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = { showStopDialog.value = false }
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
                    color = timerStateColor
                )
                LinearProgressIndicator(
                    progress = progress,
                    color = timerStateColor,
                    modifier = if (isLandscape) Modifier.width(450.dp) else Modifier.width(225.dp)
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))
            Text(
                text = viewModel.getFormattedTime(),
                style = if (isLandscape) MaterialTheme.typography.h1.copy(fontSize = 160.sp) else MaterialTheme.typography.h1
            )
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                when (viewModel.timerState.value) {
                    TimerViewModel.TimerState.WAITING_FOR_USER -> {
                        Button(
                            onClick = { viewModel.continueToNextState() },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = timerStateColor,
                                contentColor = buttonTextColour
                            )) {
                            val startLabel = stringResource(R.string.start_label)
                            var nextStateLabel = viewModel.nextState.toString()
                            if (viewModel.nextState == TimerViewModel.TimerState.LONG_BREAK) {
                                nextStateLabel = "LONG BREAK"
                            }
                            val nextText = "$startLabel $nextStateLabel" // e.g. "Start break"
                            Text(text = nextText)
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = { showResetDialog.value = true },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = timerStateColor,
                                contentColor = buttonTextColour
                            )) {
                            Text(text = stringResource(R.string.reset_button))
                        }
                        if (showResetDialog.value) {
                            ConfirmActionDialog(
                                title = stringResource(R.string.reset_timer_label),
                                message = stringResource(R.string.are_you_sure_reset_timer_label),
                                timerStateColor = timerStateColor,
                                buttonTextColour = buttonTextColour,
                                onConfirm = { viewModel.resetTimer(context) },
                                onDismiss = { showResetDialog.value = false }
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = { showStopDialog.value = true },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = timerStateColor,
                                contentColor = buttonTextColour
                            )) {
                            Text(text = stringResource(R.string.stop_button))
                        }
                    }

                    TimerViewModel.TimerState.IDLE -> {
                        if (viewModel.timeElapsedState == 0) {
                            Button(onClick = { viewModel.startTimer(context) },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = timerStateColor,
                                    contentColor = buttonTextColour
                                )) {
                                Text(text = stringResource(R.string.start_button))
                            }
                        } else {
                            Button(
                                onClick = { viewModel.resumeTimer(context) },
                                modifier = Modifier.width(125.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = timerStateColor,
                                    contentColor = buttonTextColour
                                )
                            ) {
                                Text(text = stringResource(R.string.resume_button))
                            }
                            Spacer(modifier = Modifier.width(8.dp)) // Adding a space between buttons

                            Button(onClick = { showResetDialog.value = true },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = timerStateColor,
                                    contentColor = buttonTextColour
                                )) {
                                Text(text = stringResource(R.string.reset_button))
                            }
                            if (showResetDialog.value) {
                                ConfirmActionDialog(
                                    title = stringResource(R.string.reset_timer_label),
                                    message = stringResource(R.string.are_you_sure_reset_timer_label),
                                    timerStateColor = timerStateColor,
                                    buttonTextColour = buttonTextColour,
                                    onConfirm = { viewModel.resetTimer(context) },
                                    onDismiss = { showResetDialog.value = false }
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp)) // Adding a space between buttons

                            Button(onClick = { showStopDialog.value = true },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = timerStateColor,
                                    contentColor = buttonTextColour
                                )) {
                                Text(text = stringResource(R.string.stop_button))
                            }
                        }
                    }

                    TimerViewModel.TimerState.WORK, TimerViewModel.TimerState.BREAK, TimerViewModel.TimerState.LONG_BREAK -> {
                        Button(onClick = { viewModel.pauseTimer(context) },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = timerStateColor,
                                contentColor = buttonTextColour
                            )) {
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
    timerStateColor: Color,
    buttonTextColour: Color,
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
            },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = timerStateColor,
                    contentColor = buttonTextColour
                )) {
                Text(text = stringResource(R.string.yes_label))}
        },

        dismissButton = {
            Button(onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = timerStateColor,
                    contentColor = buttonTextColour
                )) {
                Text(text = stringResource(R.string.no_label))}
        }
    )
}


class TimerFragment : Fragment() {
    private val timerViewModel: TimerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val settings by settingsViewModel.settingsFlow.collectAsState(initial = Settings())
                TimeScribeTheme(darkModeState = settings.darkMode) {
                    TimerScreen(timerViewModel)
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
