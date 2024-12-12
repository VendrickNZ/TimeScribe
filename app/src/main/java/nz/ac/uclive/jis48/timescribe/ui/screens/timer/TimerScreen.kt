package nz.ac.uclive.jis48.timescribe.ui.screens.timer

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import nz.ac.uclive.jis48.timescribe.models.TimerViewModel.TimerState
import nz.ac.uclive.jis48.timescribe.ui.theme.*

@Composable
fun TimerScreen(viewModel: TimerViewModel) {
    val context = LocalContext.current
    val showStopDialog = remember { mutableStateOf(false) }
    val showResetDialog = remember { mutableStateOf(false) }

    val currentStateDurationString = viewModel.getCurrentStateDurationString()
    val workDuration = viewModel.getCurrentWorkDuration()
    val progress = viewModel.getProgress()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val isLightTheme = MaterialTheme.colors.isLight

    val previousStateColor = viewModel.colourForPreviousState(
        previousState = viewModel.currentStateInfo.previousState,
        isLightTheme = isLightTheme
    )

    val timerStateColor = when (viewModel.currentStateInfo.currentState) {
        TimerState.WORK -> if (isLightTheme) WorkColorLight else WorkColorDark
        TimerState.BREAK -> if (isLightTheme) BreakColorLight else BreakColorDark
        TimerState.LONG_BREAK -> if (isLightTheme) LongBreakColorLight else LongBreakColorDark
        TimerState.CONTINUED_STATE -> previousStateColor
        else -> MaterialTheme.colors.onBackground
    }

    val buttonTextColor = if (isLightTheme) Color.Black else Color.White

    @Composable
    fun ShowConfirmDialogIfNeeded(
        showDialog: MutableState<Boolean>,
        title: String,
        message: String,
        dialogColor: Color,
        onConfirm: () -> Unit
    ) {
        if (showDialog.value) {
            ConfirmActionDialog(
                title = title,
                message = message,
                timerStateColour = dialogColor,
                buttonTextColour = buttonTextColor,
                onConfirm = {
                    onConfirm()
                    showDialog.value = false
                },
                onDismiss = { showDialog.value = false }
            )
        }
    }

    ShowConfirmDialogIfNeeded(
        showDialog = showStopDialog,
        title = stringResource(R.string.stop_timer_label),
        message = stringResource(R.string.are_you_sure_stop_timer_label),
        dialogColor = timerStateColor
    ) {
        viewModel.stopTimer(context)
        Toast.makeText(
            context,
            context.getString(R.string.session_saved_toast_label),
            Toast.LENGTH_SHORT
        ).show()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viewModel.timerState.value != TimerState.CONTINUED_STATE
                && viewModel.currentStateInfo.currentState != TimerState.CONTINUED_STATE
                && workDuration != 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = currentStateDurationString,
                    style = MaterialTheme.typography.h6,
                    color = timerStateColor
                )
                LinearProgressIndicator(
                    progress = progress,
                    color = timerStateColor,
                    modifier = if (isLandscape) Modifier.width(450.dp) else Modifier.width(225.dp)
                )
            } else if (viewModel.currentStateInfo.currentState == TimerState.CONTINUED_STATE) {
                Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 16.dp))
                Text(
                    text = "Continuing ${viewModel.currentStateInfo.previousStateName}",
                    style = MaterialTheme.typography.h6,
                    color = previousStateColor
                )
            }

            Spacer(modifier = Modifier.weight(0.1f))
            // The main timer text
            Text(
                text = viewModel.getFormattedTime(),
                style = if (isLandscape) MaterialTheme.typography.h1.copy(fontSize = 126.sp) else MaterialTheme.typography.h1
            )

            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                when (viewModel.timerState.value) {
                    TimerState.WAITING_FOR_USER -> {
                        val startLabel = stringResource(R.string.start_label)
                        val nextLabel = viewModel.nextState?.second ?: ""
                        val nextText = "$startLabel $nextLabel"
                        Button(
                            onClick = { viewModel.continueToNextState() },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = timerStateColor, contentColor = buttonTextColor
                            )
                        ) {
                            Text(text = nextText)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { showResetDialog.value = true },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = timerStateColor, contentColor = buttonTextColor
                            )
                        ) {
                            Text(text = stringResource(R.string.reset_button))
                        }

                        val resetDialogColor = if (viewModel.timerState.value == TimerState.CONTINUED_STATE) previousStateColor else timerStateColor
                        ShowConfirmDialogIfNeeded(
                            showDialog = showResetDialog,
                            title = stringResource(R.string.reset_timer_label),
                            message = stringResource(R.string.are_you_sure_reset_timer_label),
                            dialogColor = resetDialogColor
                        ) {
                            viewModel.resetTimer(context)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { showStopDialog.value = true },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = timerStateColor, contentColor = buttonTextColor
                            )
                        ) {
                            Text(text = stringResource(R.string.stop_button))
                        }
                    }

                    TimerState.IDLE -> {
                        if (viewModel.timeElapsedState == 0) {
                            Button(
                                onClick = { viewModel.startTimer(context) },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = timerStateColor,
                                    contentColor = buttonTextColor
                                )
                            ) {
                                Text(text = stringResource(R.string.start_button))
                            }
                        } else {
                            Button(
                                onClick = { viewModel.resumeTimer(context) },
                                modifier = Modifier.width(125.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = timerStateColor,
                                    contentColor = buttonTextColor
                                )
                            ) {
                                Text(text = stringResource(R.string.resume_button))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { showResetDialog.value = true },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = timerStateColor,
                                    contentColor = buttonTextColor
                                )
                            ) {
                                Text(text = stringResource(R.string.reset_button))
                            }

                            val resetDialogColor = if (viewModel.timerState.value == TimerState.CONTINUED_STATE) previousStateColor else timerStateColor
                            ShowConfirmDialogIfNeeded(
                                showDialog = showResetDialog,
                                title = stringResource(R.string.reset_timer_label),
                                message = stringResource(R.string.are_you_sure_reset_timer_label),
                                dialogColor = resetDialogColor
                            ) {
                                viewModel.resetTimer(context)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { showStopDialog.value = true },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = timerStateColor,
                                    contentColor = buttonTextColor
                                )
                            ) {
                                Text(text = stringResource(R.string.stop_button))
                            }

                        }
                    }

                    TimerState.WORK, TimerState.BREAK, TimerState.LONG_BREAK -> {
                        Button(
                            onClick = { viewModel.pauseTimer(context) },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = timerStateColor, contentColor = buttonTextColor
                            )
                        ) {
                            Text(text = stringResource(R.string.pause_button))
                        }
                    }

                    TimerState.CONTINUED_STATE -> {
                        Button(
                            onClick = { viewModel.pauseTimer(context) },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = previousStateColor,
                                contentColor = buttonTextColor
                            )
                        ) {
                            Text(text = stringResource(R.string.pause_button))
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(if (isLandscape) 2.dp else 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (viewModel.timerState.value == TimerState.WAITING_FOR_USER) {
                    Button(
                        onClick = { viewModel.startContinuedState() },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = previousStateColor, contentColor = buttonTextColor
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.continue_label) + " " + viewModel.currentStateInfo.previousStateName + "?"
                        )
                    }
                }

                if (viewModel.currentStateInfo.currentState == TimerState.CONTINUED_STATE
                    && viewModel.timerState.value == TimerState.IDLE) {
                    val nextStateColor = viewModel.colourForPreviousState(viewModel.wouldBeNextState, isLightTheme)
                    Button(
                        onClick = { viewModel.continueToNextState() },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = nextStateColor, contentColor = buttonTextColor
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.start_label) + " " + (viewModel.nextState?.second ?: "") + "?"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(if (isLandscape) 0.1f else 1.5f))
        }
    }
}

@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    timerStateColour: Color,
    buttonTextColour: Color,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        title = { Text(text = title) },
        text = { Text(text = message) },
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = timerStateColour,
                    contentColor = buttonTextColour
                )
            ) {
                Text(text = stringResource(R.string.yes_label))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = timerStateColour,
                    contentColor = buttonTextColour
                )
            ) {
                Text(text = stringResource(R.string.no_label))
            }
        }
    )
}


class TimerFragment : Fragment() {
    private val timerViewModel: TimerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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