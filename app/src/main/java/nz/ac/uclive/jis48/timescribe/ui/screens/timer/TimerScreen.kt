package nz.ac.uclive.jis48.timescribe.ui.screens.timer

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Paint.Align
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.NonDisposableHandle.parent
import nz.ac.uclive.jis48.timescribe.R
import nz.ac.uclive.jis48.timescribe.models.TimerViewModel
import nz.ac.uclive.jis48.timescribe.ui.theme.TimeScribeTheme
import java.util.Timer

@Composable
fun TimerScreen(paddingValues: PaddingValues, viewModel: TimerViewModel) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            Text(
                text = viewModel.getFormattedTime(),
                style = MaterialTheme.typography.h1
            )
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = { viewModel.startTimer() }) {
                    Text(text = "Start")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.pauseTimer() }) {
                    Text(text = "Pause")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.resumeTimer() }) {
                    Text(text = "Resume")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.stopTimer() }) {
                    Text(text = "Stop")
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
