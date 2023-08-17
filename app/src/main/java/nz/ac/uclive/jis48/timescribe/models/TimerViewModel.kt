package nz.ac.uclive.jis48.timescribe.models

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nz.ac.uclive.jis48.timescribe.data.*
import java.util.*

class TimerViewModel(private val settingsViewModel: SettingsViewModel,
                    private val timerRepository: TimerRepository
) : ViewModel() {

    enum class TimerState {
        IDLE, WORK, BREAK, LONG_BREAK
    }

    private var startTime: Long = 0
    private var timerJob: Job? = null
    private val timeElapsed: MutableState<Int> = mutableStateOf(0)
    var timerState: MutableState<TimerState> = mutableStateOf(TimerState.IDLE)
    private val settings: MutableState<Settings> = mutableStateOf(Settings())
    private var currentCycle: Int = 0
    val timeIsOverEvent = MutableLiveData<Boolean>()


    private var startDate: Date? = null
    private var endDate: Date? = null
    private var pauseStartTime: Date? = null
    private var totalPauseDuration: Long = 0
    private val pauseIntervals = mutableListOf<Pair<Date, Date>>()
    private var totalWorkDuration: Long = 0
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
        startTime = System.currentTimeMillis()
        startDate = Date()
        timerState.value = TimerState.WORK
        lastNonIdleState = timerState.value
        timerJob = viewModelScope.launch {
            while (true) {
                val elapsedTime = (System.currentTimeMillis() - startTime - totalPauseDuration) / 1000
                when (timerState.value) {
                    TimerState.WORK -> {
                        if (settings.value.workDuration == 0 || timeElapsed.value < settings.value.workDuration * 60) {
                            timeElapsed.value = elapsedTime.toInt()
                        } else {
                            totalWorkDuration += elapsedTime
                            timeIsOverEvent.value = true
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
                        if (settings.value.breakDuration == 0 || elapsedTime < settings.value.breakDuration * 60) {
                            timeElapsed.value = elapsedTime.toInt()
                        } else {
                            timeIsOverEvent.value = true
                            timerState.value = TimerState.WORK
                            timeElapsed.value = 0
                        }
                    }
                    TimerState.LONG_BREAK -> {
                        if (settings.value.longBreakDuration == 0 || elapsedTime < settings.value.longBreakDuration * 60) {
                            timeElapsed.value = elapsedTime.toInt()
                        } else {
                            timeIsOverEvent.value = true
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
                val pauseEndTime = Date()
                pauseIntervals.add(it to pauseEndTime)
                totalPauseDuration += pauseEndTime.time - it.time
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
        if (timerState.value == TimerState.IDLE) {
            pauseStartTime?.let {
                val pauseEndTime = Date()
                pauseIntervals.add(it to pauseEndTime)
                totalPauseDuration += pauseEndTime.time - it.time
            }
        }
        endDate = Date()
        saveSession()

        totalPauseDuration = 0
        pauseStartTime = null
        timerJob?.cancel()
        timerJob = null
        timeElapsed.value = 0
        timerState.value = TimerState.IDLE
        currentCycle = 0
    }

    fun timeIsOverHandled() {
        timeIsOverEvent.value = false
    }

    fun notifyTimeIsOver(context: Context) {
        val intent = Intent(context, TimerService::class.java).apply {
            action = ACTION_NOTIFY_TIME_IS_OVER
        }
        context.startService(intent)
    }


    private fun saveSession() {
        val session = Session(
            startDate = startDate ?: Date(),
            endDate = endDate ?: Date(),
            pauseCount = pauseIntervals.size,
            totalPauseDuration = totalPauseDuration,
            totalWorkDuration = totalWorkDuration,
            pauseIntervals = pauseIntervals
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
        val displayState = if (timerState.value == TimerState.IDLE && timeElapsedState != 0) lastNonIdleState else timerState.value
        val totalDuration = when (displayState) {
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
}

