package nz.ac.uclive.jis48.timescribe.ui.screens.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import nz.ac.uclive.jis48.timescribe.data.TimerRepository
import nz.ac.uclive.jis48.timescribe.models.HistoryViewModel
import nz.ac.uclive.jis48.timescribe.ui.theme.TimeScribeTheme

@Composable
fun HistoryScreen(paddingValues: PaddingValues, viewModel: HistoryViewModel) {
    val todaySessions = viewModel.todaySessions.value

    Column(
        modifier = Modifier.padding(paddingValues)
    ) {
        Text(text = "Today's Sessions:")
        todaySessions.forEach { session ->
            Text(text = "Session started at ${session.startDate}")
        }
    }
}


class HistoryFragment(val historyViewModel: HistoryViewModel) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                TimeScribeTheme {
                    HistoryScreen(paddingValues = PaddingValues(0.dp), viewModel = historyViewModel)
                }
            }
        }
    }
}
