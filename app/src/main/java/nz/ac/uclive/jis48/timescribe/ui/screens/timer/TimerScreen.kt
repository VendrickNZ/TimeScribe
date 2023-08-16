package nz.ac.uclive.jis48.timescribe.ui.screens.timer

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.progressSemantics
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import nz.ac.uclive.jis48.timescribe.R
import nz.ac.uclive.jis48.timescribe.models.SettingsViewModel
import nz.ac.uclive.jis48.timescribe.models.TimerViewModel
import nz.ac.uclive.jis48.timescribe.models.TimerViewModelFactory
import nz.ac.uclive.jis48.timescribe.ui.theme.TimeScribeTheme

@Composable
fun TimerScreen(paddingValues: PaddingValues, viewModel: TimerViewModel) {
    val showDialog = remember { mutableStateOf(false) }
    if (showDialog.value) {
        AlertDialog(
            title = { Text(text = "Stop Timer") },
            text = { Text(text = "Are you sure you want to stop the timer?") },
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                Button(onClick = {
                    viewModel.stopTimer()
                    showDialog.value = false
                }) {
                    Text(text = "Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text(text = "No")
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
            Text(
                text = "Total Duration: ${viewModel.getTotalDuration()}",
                style = MaterialTheme.typography.h6
            )
            LinearProgressIndicator(
                progress = viewModel.getProgress(),
                color = Color(ContextCompat.getColor(LocalContext.current, R.color.gentle_orange))
            )

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
                    TimerViewModel.TimerState.IDLE -> {
                        if (viewModel.timeElapsedState == 0) {
                            Button(onClick = { viewModel.startTimer() }) {
                                Text(text = stringResource(R.string.start_button))
                            }
                        } else {
                            Button(onClick = { viewModel.resumeTimer() }) {
                                Text(text = stringResource(R.string.resume_button))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { viewModel.resetTimer() }) {
                                Text(text = stringResource(R.string.reset_button))
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { showDialog.value = true }) {
                                Text(text = stringResource(R.string.stop_button))
                            }
                        }
                    }
                    TimerViewModel.TimerState.WORK, TimerViewModel.TimerState.BREAK, TimerViewModel.TimerState.LONG_BREAK -> {
                        Button(onClick = { viewModel.pauseTimer() }) {
                            Text(text = stringResource(R.string.pause_button))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1.5f))
        }
    }
}




class TimerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val settingsViewModel = viewModel<SettingsViewModel>()
                val factory = TimerViewModelFactory(requireContext(), settingsViewModel)
                val timerViewModel: TimerViewModel = viewModel(factory = factory)
                TimeScribeTheme {
                    TimerScreen(PaddingValues(0.dp), viewModel())
                }
            }
        }
    }
}

fun Context.findFragmentActivity(): FragmentActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null

}
