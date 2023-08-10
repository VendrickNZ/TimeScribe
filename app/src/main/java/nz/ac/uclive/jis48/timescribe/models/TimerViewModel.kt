package nz.ac.uclive.jis48.timescribe.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {
    public val timeElapsed: MutableState<Int> = mutableStateOf(0)

    fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                timeElapsed.value += 1
            }
        }
    }

    fun stopTimer() {
        viewModelScope.cancel()
    }

    fun getFormattedTime(): String {
        val minutes = timeElapsed.value / 60
        val seconds = timeElapsed.value % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}