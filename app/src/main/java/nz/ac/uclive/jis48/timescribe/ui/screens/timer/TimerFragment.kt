package nz.ac.uclive.jis48.timescribe.ui.screens.timer

import android.graphics.Paint.Align
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import nz.ac.uclive.jis48.timescribe.R
import nz.ac.uclive.jis48.timescribe.models.TimerViewModel
import nz.ac.uclive.jis48.timescribe.ui.theme.TimeScribeTheme

@Composable
fun TimerScreen(paddingValues: PaddingValues) {
    val viewModel: TimerViewModel = viewModel()
    val timeElapsed = viewModel.timeElapsed.value

    LaunchedEffect(Unit) {
        viewModel.startTimer()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text = viewModel.getFormattedTime(), style = MaterialTheme.typography.h1)
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
                    TimerScreen(PaddingValues(0.dp))
                }
            }
        }
    }
}
