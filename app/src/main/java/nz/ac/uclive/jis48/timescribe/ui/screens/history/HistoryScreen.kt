package nz.ac.uclive.jis48.timescribe.ui.screens.history

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import nz.ac.uclive.jis48.timescribe.models.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat.startActivity
import nz.ac.uclive.jis48.timescribe.R
import nz.ac.uclive.jis48.timescribe.data.Session
import nz.ac.uclive.jis48.timescribe.data.Settings
import nz.ac.uclive.jis48.timescribe.models.SettingsViewModel
import nz.ac.uclive.jis48.timescribe.ui.theme.*

@Composable
fun HistoryScreen(paddingValues: PaddingValues, historyViewModel: HistoryViewModel, settingsViewModel: SettingsViewModel, onShareSession: (Session) -> Unit) {
    val timeFormatter = SimpleDateFormat("h:mma", Locale.getDefault())
    val selectedDate = remember { mutableStateOf(Date()) }
    val selectedSessions by historyViewModel.selectedSessions.observeAsState(initial = emptyList())
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val dateStyle = if (isLandscape) MaterialTheme.typography.h4 else MaterialTheme.typography.h5
    val datePadding = if (isLandscape) 1.dp else 8.dp
    val dayPadding = if (isLandscape) 16.dp else 8.dp

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(selectedDate.value),
            style = dateStyle,
            modifier = Modifier.padding(datePadding)
        )
        Modifier.padding(8.dp)
        WeeklyCalendar(selectedDate = selectedDate.value, dayPadding, settingsViewModel = settingsViewModel) { date ->
            selectedDate.value = date
            historyViewModel.loadSessionsForDate(date)
        }

        LazyColumn {
            items(selectedSessions) { session ->
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
                                text = stringResource(R.string.pause_count_label)  +  " ${session.pauseCount}",
                                style = MaterialTheme.typography.body2
                            )
                            val totalPauseDurationInSeconds = session.totalPauseDuration / 1000
                            Text(
                                text = stringResource(R.string.total_pause_duration_label) + " ${totalPauseDurationInSeconds}s",
                                style = MaterialTheme.typography.body2
                            )

                            session.pauseIntervals.forEach { interval ->
                                Text(
                                    text = stringResource(R.string.paused_from_label) + " " + timeFormatter.format(interval.first) + " "
                                            + stringResource(R.string.to_label) + " " +  timeFormatter.format(interval.second),
                                    style = MaterialTheme.typography.body2
                                )
                            }
                            Button(onClick = { onShareSession(session) }) {
                                Text("Share")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyCalendar(selectedDate: Date, dayPadding: Dp, settingsViewModel: SettingsViewModel, onSelectDate: (Date) -> Unit) {
    val currentCalendar = Calendar.getInstance()
    val selectedCalendar = Calendar.getInstance().apply {
        time = selectedDate
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    }

    selectedCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

    val daysOfWeek = listOf(
        R.string.mon_label,
        R.string.tue_label,
        R.string.wed_label,
        R.string.thu_label,
        R.string.fri_label,
        R.string.sat_label,
        R.string.sun_label
    )

    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
        for (i in 0 until 7) {
            val dayCalendar = selectedCalendar.clone() as Calendar

            val dayOfMonth = dayCalendar.get(Calendar.DAY_OF_MONTH)
            val isToday = currentCalendar.get(Calendar.DAY_OF_YEAR) == dayCalendar.get(Calendar.DAY_OF_YEAR) &&
                    currentCalendar.get(Calendar.YEAR) == dayCalendar.get(Calendar.YEAR)

            val isClickable = dayCalendar.time.before(currentCalendar.time) || isToday

            val settings = settingsViewModel.settingsFlow.collectAsState(initial = Settings()).value
            val darkMode = settings.darkMode

            val textColor = when {
                selectedDate == dayCalendar.time -> if (darkMode) green else darkGreen
                isToday -> if (darkMode) lightBlue else darkBlue
                isClickable -> if (darkMode) white else black
                else -> if (darkMode) darkGray else gray
            }

            Box(
                modifier = Modifier
                    .clickable(enabled = isClickable) { if (isClickable) onSelectDate(dayCalendar.time) }
                    .padding(dayPadding)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(daysOfWeek[i]), color = textColor)
                    Text(text = "$dayOfMonth", color = textColor)
                }
            }

            selectedCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}

fun shareSession(context: Context, session: Session) {
    val sessionDetails = """
        Start Time: ${session.startDate}
        End Time: ${session.endDate}
        Pause Count: ${session.pauseCount}
        Total Pause Duration: ${session.totalPauseDuration / 1000}s
        // Add more details as needed
    """.trimIndent()

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Session Details")
        putExtra(Intent.EXTRA_TEXT, sessionDetails)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Session via"))
}


class HistoryFragment(val historyViewModel: HistoryViewModel, val settingsViewModel: SettingsViewModel) : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TimeScribeTheme {
                    HistoryScreen(
                        paddingValues = PaddingValues(0.dp),
                        historyViewModel = historyViewModel,
                        settingsViewModel = settingsViewModel,
                        onShareSession = { session ->
                            shareSession(
                                requireContext(),
                                session
                            )
                        }
                    )
                }
            }
        }
    }
}