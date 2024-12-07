package nz.ac.uclive.jis48.timescribe.models


import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nz.ac.uclive.jis48.timescribe.R
import nz.ac.uclive.jis48.timescribe.data.*
import nz.ac.uclive.jis48.timescribe.ui.theme.BreakColorDark
import nz.ac.uclive.jis48.timescribe.ui.theme.BreakColorLight
import nz.ac.uclive.jis48.timescribe.ui.theme.LongBreakColorDark
import nz.ac.uclive.jis48.timescribe.ui.theme.LongBreakColorLight
import nz.ac.uclive.jis48.timescribe.ui.theme.WorkColorDark
import nz.ac.uclive.jis48.timescribe.ui.theme.WorkColorLight
import nz.ac.uclive.jis48.timescribe.utils.AlarmReceiver
import java.util.*
import android.provider.Settings as AndroidSettings

class TimerViewModel(
    private val settingsViewModel: SettingsViewModel, private val timerRepository: TimerRepository
) : ViewModel() {

    enum class TimerState {
        IDLE, WORK, BREAK, LONG_BREAK, WAITING_FOR_USER
    }

    val currentStateInfo = StateInfo(
        previousState = TimerState.WORK,
        currentState = TimerState.WORK,
        previousStateName = "Work",
        currentStateName = "Work",
        duration = 25
    )

    private var startTime: Long = 0
    private var timerJob: Job? = null
    private val timeElapsed: MutableState<Int> = mutableIntStateOf(0)
    var timerState: MutableState<TimerState> = mutableStateOf(TimerState.IDLE)
    private val settings: MutableState<Settings> = mutableStateOf(Settings())
    private var currentCycle: Int = 0
    val timeIsOverEvent = MutableLiveData<Boolean>()
    var nextState: TimerState? = null

    private var startDate: Date? = null
    private var endDate: Date? = null
    private var pauseStartTime: Date? = null
    private var totalPauseDuration: Long = 0
    private val pauseIntervals = mutableListOf<Pair<Date, Date>>()
    private var totalWorkDuration: Long = 0
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

    fun startTimer(context: Context) {
        if (timerState.value != TimerState.IDLE) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                checkAndRequestExactAlarmPermission(context)
                return
            }
        }
        resetPauseDuration()

        val notifyTimeInMillis =
            System.currentTimeMillis() + (settings.value.workDuration * 60 * 1000)

        if (settings.value.workDuration != 0) {
            scheduleAlarm(context, notifyTimeInMillis)
        }

        startTime = System.currentTimeMillis()
        startDate = Date()
        if (currentStateInfo.currentState == TimerState.WORK) {
            timerState.value = TimerState.WORK
        }
        timerJob = viewModelScope.launch {
            while (true) {
                if (timerState.value == TimerState.WAITING_FOR_USER) {
                    delay(1000)
                    continue
                }
                val elapsedTime =
                    ((System.currentTimeMillis() - startTime - totalPauseDuration) / 1000)
                Log.d("TimerViewModel", "Elapsed time: $elapsedTime")
                updateElapsedTime(elapsedTime)
                delay(1000)
            }
        }
    }

    private fun updateElapsedTime(elapsedTime: Long) {
        when (timerState.value) {
            TimerState.WORK -> {
                if (settings.value.workDuration == 0 || elapsedTime < settings.value.workDuration * 60) {
                    timeElapsed.value = elapsedTime.toInt()
                } else {
                    totalWorkDuration += elapsedTime
                    onStateTransition(
                        if (currentCycle + 1 >= settings.value.cyclesBeforeLongBreak) TimerState.LONG_BREAK
                        else TimerState.BREAK
                    )
                }
            }

            TimerState.BREAK -> {
                if (settings.value.breakDuration == 0 || elapsedTime < settings.value.breakDuration * 60) {
                    timeElapsed.value = elapsedTime.toInt()
                } else {
                    onStateTransition(TimerState.WORK)
                }
            }

            TimerState.LONG_BREAK -> {
                if (settings.value.longBreakDuration == 0 || elapsedTime < settings.value.longBreakDuration * 60) {
                    timeElapsed.value = elapsedTime.toInt()
                } else {
                    onStateTransition(TimerState.WORK)
                }
            }

            else -> {}
        }
    }

    private fun onStateTransition(next: TimerState) {
        currentStateInfo.previousState = currentStateInfo.currentState
        currentStateInfo.previousStateName = currentStateInfo.currentStateName
        currentStateInfo.currentState = next
        currentStateInfo.currentStateName = getCurrentStateName(next)
        currentStateInfo.duration = getStateDurationInt(next)

        nextState = next
        if (next == TimerState.WORK && timerState.value == TimerState.LONG_BREAK) {
            currentCycle = 0
        } else if (next != TimerState.WORK) {
            currentCycle++
        }
        stateEndCleanup()
    }

    private fun stateEndCleanup() {
        timeIsOverEvent.value = true
        timerState.value = TimerState.WAITING_FOR_USER
        timeElapsed.value = 0
        startTime = System.currentTimeMillis()
        resetPauseDuration()
    }

    fun pauseTimer(context: Context) {
        if (timerState.value == TimerState.WORK || timerState.value == TimerState.BREAK || timerState.value == TimerState.LONG_BREAK) {
            cancelAlarm(context)
            pauseStartTime = Date()
            timerState.value = TimerState.IDLE
        }
    }

    fun resumeTimer(context: Context) {
        if (timerState.value == TimerState.IDLE) {
            val totalDuration = when (currentStateInfo.currentState) {
                TimerState.WORK -> settings.value.workDuration * 60
                TimerState.BREAK -> settings.value.breakDuration * 60
                TimerState.LONG_BREAK -> settings.value.longBreakDuration * 60
                else -> 0
            }

            val remainingTime = (totalDuration - timeElapsed.value) * 1000
            val notifyTimeInMillis = System.currentTimeMillis() + remainingTime
            scheduleAlarm(context, notifyTimeInMillis)

            pauseStartTime?.let {
                val pauseEndTime = Date()
                pauseIntervals.add(it to pauseEndTime)
                totalPauseDuration += pauseEndTime.time - it.time
            }

            startTime = System.currentTimeMillis() - (timeElapsed.value * 1000) - totalPauseDuration

            timerState.value = currentStateInfo.currentState
        }
    }

    fun resetTimer(context: Context) {
        cancelAlarm(context)
        if (timerState.value == TimerState.IDLE || timerState.value == TimerState.WAITING_FOR_USER) {
            totalPauseDuration = 0
            pauseStartTime = null
            timerJob?.cancel()
            timerJob = null
            timeElapsed.value = 0
            timerState.value = TimerState.IDLE
            currentCycle = 0


            currentStateInfo.previousState = TimerState.IDLE
            currentStateInfo.currentState = TimerState.WORK // Resets to work state
            currentStateInfo.previousStateName = "Idle"
            currentStateInfo.currentStateName = "Work"
            nextState = TimerState.WORK
        }
    }

    fun stopTimer(context: Context) {
        cancelAlarm(context)
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
        currentStateInfo.currentState = TimerState.WORK // Resets to work state
    }

    private fun resetPauseDuration() {
        totalPauseDuration = 0
        pauseStartTime = null
        pauseIntervals.clear()
    }

    private fun scheduleAlarm(context: Context, timeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "nz.ac.uclive.jis48.timescribe.ALARM_ACTION"
        }
        val pendingIntent =
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
            )
        } catch (e: SecurityException) {
            Log.e("TimerViewModel", "Failed to schedule exact alarm: ${e.message}")
            // TODO: Optionally inform the user
        }
    }

    private fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val flags = PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
        alarmManager.cancel(pendingIntent)
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

    private fun refreshSessions() {
        sessions.value = timerRepository.loadTodaySessions()
    }

    fun getFormattedTime(): String {
        val hours = timeElapsedState / 3600
        val minutes = (timeElapsedState % 3600) / 60
        val seconds = timeElapsedState % 60

        return if (hours > 0) {
            String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
        }
    }

    fun getProgress(): Float {
        val totalDuration = when (timerState.value.takeIf { it != TimerState.IDLE }
            ?: currentStateInfo.currentState) {
            TimerState.WORK -> settings.value.workDuration * 60
            TimerState.BREAK -> settings.value.breakDuration * 60
            TimerState.LONG_BREAK -> settings.value.longBreakDuration * 60
            else -> 0
        }
        return if (totalDuration > 0) timeElapsed.value.toFloat() / totalDuration else 0f
    }

    fun getCurrentStateDurationString(): String {
        val currentState = if (timerState.value == TimerState.IDLE) {
            currentStateInfo.currentState
        } else if (timerState.value == TimerState.WAITING_FOR_USER) {
            nextState ?: currentStateInfo.currentState
        } else {
            timerState.value
        }
        return when (currentState) {
            TimerState.WORK -> formatDuration("Work", settings.value.workDuration)
            TimerState.BREAK -> formatDuration("Break", settings.value.breakDuration)
            TimerState.LONG_BREAK -> formatDuration("Long Break", settings.value.longBreakDuration)
            else -> formatDuration("Work", settings.value.workDuration)
        }
    }

    private fun getStateDurationInt(state: TimerState): Int {
        return when (state) {
            TimerState.WORK -> settings.value.workDuration
            TimerState.BREAK -> settings.value.breakDuration
            TimerState.LONG_BREAK -> settings.value.longBreakDuration
            else -> 0
        }
    }

    private fun getCurrentStateName(state: TimerState): String {
        return when (state) {
            TimerState.WORK -> "Work"
            TimerState.BREAK -> "Break"
            TimerState.LONG_BREAK -> "Long Break"
            TimerState.IDLE -> "Idle"
            TimerState.WAITING_FOR_USER -> "Waiting"
        }
    }

    private fun formatDuration(stateLabel: String, duration: Int): String {
        val unit = if (duration == 1) "minute" else "minutes"
        return "$stateLabel: $duration $unit"
    }

    fun colourForPreviousState(previousState: TimerState, isLightTheme: Boolean): Color {
        return when (previousState) {
            TimerState.WORK -> if (isLightTheme) WorkColorLight else WorkColorDark
            TimerState.BREAK -> if (isLightTheme) BreakColorLight else BreakColorDark
            TimerState.LONG_BREAK -> if (isLightTheme) LongBreakColorLight else LongBreakColorDark
            else -> Color.Gray
        }
    }

    fun getCurrentWorkDuration(): Int {
        return settings.value.workDuration
    }

    fun continueToNextState() {
        resetPauseDuration()
        startTime = System.currentTimeMillis()
        timerState.value = nextState ?: TimerState.IDLE
        currentStateInfo.previousState = currentStateInfo.currentState
        currentStateInfo.currentState = timerState.value
        nextState = null
        timeElapsed.value = 0
    }


    private fun checkAndRequestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.permission_required_title))
                    .setMessage(context.getString(R.string.permission_required_message))
                    .setPositiveButton(context.getString(R.string.open_settings)) { _, _ ->
                        val intent =
                            Intent(AndroidSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                        context.startActivity(intent)
                    }.setNegativeButton(context.getString(R.string.cancel_label), null).show()
            }
        }
    }
}

