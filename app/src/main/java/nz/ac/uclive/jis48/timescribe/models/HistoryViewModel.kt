package nz.ac.uclive.jis48.timescribe.models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import nz.ac.uclive.jis48.timescribe.data.Session
import nz.ac.uclive.jis48.timescribe.data.TimerRepository

class HistoryViewModel(private val timerRepository: TimerRepository) : ViewModel() {
    val todaySessions = mutableStateOf<List<Session>>(emptyList())

    init {
        todaySessions.value = timerRepository.loadTodaySessions()
    }
}
