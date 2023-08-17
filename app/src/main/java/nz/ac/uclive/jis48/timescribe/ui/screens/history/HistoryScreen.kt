package nz.ac.uclive.jis48.timescribe.ui.screens.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import nz.ac.uclive.jis48.timescribe.models.HistoryViewModel
import nz.ac.uclive.jis48.timescribe.ui.theme.TimeScribeTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(paddingValues: PaddingValues, viewModel: HistoryViewModel) {
    val todaySessions = viewModel.todaySessions.value
    val dateFormatter = SimpleDateFormat("EEEE d MMMM", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("h:mma", Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dateFormatter.format(Date()),
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(8.dp)
        )
        Modifier.padding(8.dp)

        LazyColumn {
            items(todaySessions) { session ->
                val expanded = remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier
                        .clickable { expanded.value = !expanded.value }
                        .padding(8.dp)
                        .widthIn(min = 250.dp),

                    ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {
                        Text(
                            text = "${timeFormatter.format(session.startDate)} - ${
                                timeFormatter.format(
                                    session.endDate
                                )
                            }",
                            style = MaterialTheme.typography.h6,
                        )

                        if (expanded.value) {
                            Text(
                                text = "Pause Count: ${session.pauseCount}",
                                style = MaterialTheme.typography.body2
                            )
                            Text(
                                text = "Total Pause Duration: ${session.totalPauseDuration}ms",
                                style = MaterialTheme.typography.body2
                            )

                            session.pauseIntervals.forEach { interval ->
                                Text(
                                    text = "Paused from ${timeFormatter.format(interval.first)} to ${
                                        timeFormatter.format(
                                            interval.second
                                        )
                                    }",
                                    style = MaterialTheme.typography.body2
                                )
                            }
                        }
                    }
                }
            }
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