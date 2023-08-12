package nz.ac.uclive.jis48.timescribe.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {
    enum class TimerState {
        STOPPED, RUNNING, PAUSED
    }
    private var timerJob: Job? = null
    val timeElapsed: MutableState<Int> = mutableStateOf(0)
    var timerState: MutableState<TimerState> = mutableStateOf(TimerState.STOPPED)


    val timeElapsedState: Int
        get() = timeElapsed.value

    fun startTimer() {
        if (timerState.value == TimerState.RUNNING) return
        timerState.value = TimerState.RUNNING
        timerJob = viewModelScope.launch {
            while (true) {
                if (timerState.value == TimerState.RUNNING) {
                    timeElapsed.value += 1
                }
                delay(1000)
            }
        }
    }

    fun pauseTimer() {
        timerState.value = TimerState.PAUSED
    }

    fun resumeTimer() {
        timerState.value = TimerState.RUNNING
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        timeElapsed.value = 0
        timerState.value = TimerState.STOPPED
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

}

