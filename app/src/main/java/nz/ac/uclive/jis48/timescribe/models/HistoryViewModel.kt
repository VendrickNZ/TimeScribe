package nz.ac.uclive.jis48.timescribe.models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import nz.ac.uclive.jis48.timescribe.data.Session
import nz.ac.uclive.jis48.timescribe.data.TimerRepository

class HistoryViewModel(private val timerRepository: TimerRepository,
                    private val timerViewModel: TimerViewModel
) : ViewModel() {

    val todaySessions = timerViewModel.sessions

    init {
        refreshSessions()
    }

    private fun refreshSessions() {
        todaySessions.value = timerRepository.loadTodaySessions()
    }
}
