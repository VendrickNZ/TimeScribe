package nz.ac.uclive.jis48.timescribe.models

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import nz.ac.uclive.jis48.timescribe.data.Session
import nz.ac.uclive.jis48.timescribe.data.TimerRepository
import java.util.*

class HistoryViewModel(private val timerRepository: TimerRepository,
                    private val timerViewModel: TimerViewModel
) : ViewModel() {

    val todaySessions = timerViewModel.sessions
    private val _selectedSessions = MutableLiveData<List<Session>>(emptyList())
    val selectedSessions: LiveData<List<Session>> = _selectedSessions

    init {
        refreshSessions()
    }

    private fun refreshSessions() {
        todaySessions.value = timerRepository.loadTodaySessions()
    }

    fun loadSessionsForDate(selectedDate: Date) {
        _selectedSessions.value = timerRepository.loadSessionsForDate(selectedDate)
    }

}
