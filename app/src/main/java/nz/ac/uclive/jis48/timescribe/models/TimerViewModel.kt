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
    val timeElapsed: MutableState<Int> = mutableStateOf(0)
    var timerState: MutableState<TimerState> = mutableStateOf(TimerState.IDLE)
    private val settings: MutableState<Settings> = mutableStateOf(Settings())
    private var currentCycle: Int = 0

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
        Log.d("TimerViewModel", "startTimer called, current state: ${timerState.value}")
        if (timerState.value != TimerState.IDLE) return
        timerState.value = TimerState.WORK
        timerJob = viewModelScope.launch {
            while (true) {
                Log.d("TimerViewModel", "inside loop, current timeElapsed: ${timeElapsed.value}")
                when (timerState.value) {
                    TimerState.WORK -> {
                        if (timeElapsed.value < settings.value.workDuration * 60) {
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
                        if (timeElapsed.value < settings.value.breakDuration * 60) {
                            timeElapsed.value += 1
                        } else {
                            timerState.value = TimerState.WORK
                            timeElapsed.value = 0
                        }
                    }
                    TimerState.LONG_BREAK -> {
                        if (timeElapsed.value < settings.value.longBreakDuration * 60) {
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
            timerState.value = TimerState.IDLE
        }
    }

    fun resumeTimer() {
        if (timerState.value == TimerState.IDLE) timerState.value = TimerState.WORK
    }

    fun resetTimer() {
        if (timerState.value == TimerState.IDLE) {
            timerJob?.cancel()
            timerJob = null
            timeElapsed.value = 0
            timerState.value = TimerState.IDLE
        }
    }


    fun stopTimer() {
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
            startDate = Date(), // Fill with the correct data
            startTime = "startTime",
            pauseCount = 0,
            totalPauseDuration = 0,
            endTime = "endTime",
            totalTime = 0
        )
        timerRepository.saveSession(session)
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

    fun getTotalDuration(): String {
        return when (timerState.value) {
            TimerState.WORK -> "${settings.value.workDuration} mins"
            TimerState.BREAK -> "${settings.value.breakDuration} mins"
            TimerState.LONG_BREAK -> "${settings.value.longBreakDuration} mins"
            else -> ""
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
}

