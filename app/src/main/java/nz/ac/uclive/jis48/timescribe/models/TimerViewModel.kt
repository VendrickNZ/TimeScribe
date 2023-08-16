package nz.ac.uclive.jis48.timescribe.models

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nz.ac.uclive.jis48.timescribe.data.Session
import nz.ac.uclive.jis48.timescribe.data.Settings
import nz.ac.uclive.jis48.timescribe.data.TimerRepository
import java.util.*

class TimerViewModel(private val settingsViewModel: SettingsViewModel,
                    private val timerRepository: TimerRepository
) : ViewModel() {

    enum class TimerState {
        IDLE, WORK, BREAK, LONG_BREAK
    }

    private var timerJob: Job? = null
    private val timeElapsed: MutableState<Int> = mutableStateOf(0)
    var timerState: MutableState<TimerState> = mutableStateOf(TimerState.IDLE)
    private val settings: MutableState<Settings> = mutableStateOf(Settings())
    private var currentCycle: Int = 0

    private var startDate: Date? = null
    private var endDate: Date? = null
    private var pauseStartTime: Date? = null
    private var totalPauseDuration: Long = 0
    private var lastNonIdleState: TimerState? = TimerState.WORK


    val sessions = mutableStateOf<List<Session>>(emptyList())


    init {
        viewModelScope.launch {
            settingsViewModel.settingsFlow.collect { newSettings ->
                settings.value = newSettings
            }
        }
    }

    val timeElapsedState: Int
        get() = timeElapsed.value

    fun startTimer() {
        if (timerState.value != TimerState.IDLE) return
        startDate = Date()
        timerState.value = TimerState.WORK
        lastNonIdleState = timerState.value
        timerJob = viewModelScope.launch {
            while (true) {
                when (timerState.value) {
                    TimerState.WORK -> {
                        if (settings.value.workDuration == 0 || timeElapsed.value < settings.value.workDuration * 60) {
                            timeElapsed.value += 1
                        } else {
                            currentCycle++
                            if (currentCycle == settings.value.cyclesBeforeLongBreak) {
                                timerState.value = TimerState.LONG_BREAK
                                currentCycle = 0
                            } else {
                                timerState.value = TimerState.BREAK
                            }
                            timeElapsed.value = 0
                        }
                    }
                    TimerState.BREAK -> {
                        if (settings.value.breakDuration == 0 || timeElapsed.value < settings.value.breakDuration * 60) {
                            timeElapsed.value += 1
                        } else {
                            timerState.value = TimerState.WORK
                            timeElapsed.value = 0
                        }
                    }
                    TimerState.LONG_BREAK -> {
                        if (settings.value.longBreakDuration == 0 || timeElapsed.value < settings.value.longBreakDuration * 60) {
                            timeElapsed.value += 1
                        } else {
                            timerState.value = TimerState.WORK
                            timeElapsed.value = 0
                        }
                    }
                    else -> {}
                }
                delay(1000)
            }
        }
    }


    fun pauseTimer() {
        if (timerState.value == TimerState.WORK || timerState.value == TimerState.BREAK || timerState.value == TimerState.LONG_BREAK) {
            lastNonIdleState = timerState.value
            pauseStartTime = Date()
            timerState.value = TimerState.IDLE
        }
    }

    fun resumeTimer() {
        if (timerState.value == TimerState.IDLE) {
            pauseStartTime?.let {
                totalPauseDuration += Date().time - it.time
            }
            timerState.value = lastNonIdleState ?: TimerState.WORK
        }
    }

    fun resetTimer() {
        if (timerState.value == TimerState.IDLE) {
            totalPauseDuration = 0
            pauseStartTime = null
            timerJob?.cancel()
            timerJob = null
            timeElapsed.value = 0
            timerState.value = TimerState.IDLE
        }
    }

    fun stopTimer() {
        totalPauseDuration = 0
        pauseStartTime = null
        endDate = Date(startDate?.time ?: (0 + timeElapsedState * 1000L + totalPauseDuration))
        timerJob?.cancel()
        timerJob = null
        timeElapsed.value = 0
        timerState.value = TimerState.IDLE
        Log.d("$currentCycle cycles completed", "stopTimer: ")
        currentCycle = 0
        saveSession()
    }


    private fun saveSession() {
        val session = Session(
            startDate = startDate ?: Date(),
            endDate = endDate ?: Date(),
            pauseCount = 0,
            totalPauseDuration = totalPauseDuration
        )
        timerRepository.saveSession(session)
        refreshSessions()
    }

    fun refreshSessions() {
        sessions.value = timerRepository.loadTodaySessions()
    }

    fun getFormattedTime(): String {
        val hours = timeElapsedState / 3600
        val minutes = (timeElapsedState % 3600) / 60
        val seconds = timeElapsedState % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun getProgress(): Float {
        val totalDuration = when (timerState.value) {
            TimerState.WORK -> settings.value.workDuration * 60
            TimerState.BREAK -> settings.value.breakDuration * 60
            TimerState.LONG_BREAK -> settings.value.longBreakDuration * 60
            else -> 0
        }
        return if (totalDuration > 0) timeElapsed.value.toFloat() / totalDuration else 0f
    }

    fun getCurrentStateDuration(): String {
        val displayState = if (timerState.value == TimerState.IDLE && timeElapsedState != 0) lastNonIdleState else timerState.value
                return when (displayState) {
            TimerState.WORK -> "Work: ${settings.value.workDuration} mins"
            TimerState.BREAK -> "Break: ${settings.value.breakDuration} mins"
            TimerState.LONG_BREAK -> "Long Break: ${settings.value.longBreakDuration} mins"
            else -> "Work: ${settings.value.workDuration} mins"
        }
    }

    fun getCurrentWorkDuration(): Int {
        return settings.value.workDuration
    }

    fun getCurrentBreakDuration(): Int {
        return settings.value.breakDuration
    }

    fun getCurrentLongBreakDuration(): Int {
        return settings.value.longBreakDuration
    }
}

