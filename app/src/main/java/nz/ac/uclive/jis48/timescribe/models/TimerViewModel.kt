package nz.ac.uclive.jis48.timescribe.models

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nz.ac.uclive.jis48.timescribe.data.*
import nz.ac.uclive.jis48.timescribe.utils.AlarmReceiver
import java.util.*

class TimerViewModel(private val settingsViewModel: SettingsViewModel,
                    private val timerRepository: TimerRepository
) : ViewModel() {

    enum class TimerState {
        IDLE, WORK, BREAK, LONG_BREAK, WAITING_FOR_USER
    }

    private var startTime: Long = 0
    private var timerJob: Job? = null
    private val timeElapsed: MutableState<Int> = mutableStateOf(0)
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

    fun startTimer(context: Context) {
        if (timerState.value != TimerState.IDLE) return
        val notifyTimeInMillis = System.currentTimeMillis() + (settings.value.workDuration * 60 * 1000)
        scheduleAlarm(context, notifyTimeInMillis)
        startTime = System.currentTimeMillis()
        startDate = Date()
        timerState.value = TimerState.WORK
        lastNonIdleState = timerState.value
        timerJob = viewModelScope.launch {
            while (true) {
                if (timerState.value == TimerState.WAITING_FOR_USER) {
                    delay(1000)
                    continue
                }
                val elapsedTime = (System.currentTimeMillis() - startTime - totalPauseDuration) / 1000
                when (timerState.value) {
                    TimerState.WORK -> {
                        if (settings.value.workDuration == 0 || timeElapsed.value < settings.value.workDuration * 60) {
                            timeElapsed.value = elapsedTime.toInt()
                        } else {
                            totalWorkDuration += elapsedTime
                            timeIsOverEvent.value = true
                            nextState = if (currentCycle == settings.value.cyclesBeforeLongBreak) TimerState.LONG_BREAK else TimerState.BREAK
                            timerState.value = TimerState.WAITING_FOR_USER
                            currentCycle++
                            timeElapsed.value = 0
                            startTime = System.currentTimeMillis()
                            nextState = if (currentCycle == settings.value.cyclesBeforeLongBreak) {
                                TimerState.LONG_BREAK
                            } else {
                                TimerState.BREAK
                            }
                            timerState.value = TimerState.WAITING_FOR_USER
                            currentCycle++
                            timeElapsed.value = 0
                            startTime = System.currentTimeMillis()

                        }
                    }
                    TimerState.BREAK -> {
                        if (settings.value.breakDuration == 0 || elapsedTime < settings.value.breakDuration * 60) {
                            timeElapsed.value = elapsedTime.toInt()
                        } else {
                            nextState = TimerState.WORK
                            timerState.value = TimerState.WAITING_FOR_USER
                            timeIsOverEvent.value = true
                            timeElapsed.value = 0
                        }
                    }
                    TimerState.LONG_BREAK -> {
                        if (settings.value.longBreakDuration == 0 || elapsedTime < settings.value.longBreakDuration * 60) {
                            timeElapsed.value = elapsedTime.toInt()
                        } else {
                            nextState = TimerState.WORK
                            timerState.value = TimerState.WAITING_FOR_USER
                            timeIsOverEvent.value = true
                            timeElapsed.value = 0
                        }
                    }
                    else -> {}
                }
                delay(1000)
            }
        }
    }

    fun pauseTimer(context: Context) {
        if (timerState.value == TimerState.WORK || timerState.value == TimerState.BREAK || timerState.value == TimerState.LONG_BREAK) {
            cancelAlarm(context)
            lastNonIdleState = timerState.value
            pauseStartTime = Date()
            timerState.value = TimerState.IDLE
        }
    }

    fun resumeTimer(context: Context) {
        if (timerState.value == TimerState.IDLE) {
            val remainingTime = (settings.value.workDuration * 60 - timeElapsed.value) * 1000
            val notifyTimeInMillis = System.currentTimeMillis() + remainingTime
            scheduleAlarm(context, notifyTimeInMillis)
            pauseStartTime?.let {
                val pauseEndTime = Date()
                pauseIntervals.add(it to pauseEndTime)
                totalPauseDuration += pauseEndTime.time - it.time
            }
            timerState.value = lastNonIdleState ?: TimerState.WORK
        }
    }

    fun resetTimer(context: Context) {
        cancelAlarm(context)
        if (timerState.value == TimerState.IDLE) {
            totalPauseDuration = 0
            pauseStartTime = null
            timerJob?.cancel()
            timerJob = null
            timeElapsed.value = 0
            timerState.value = TimerState.IDLE
            currentCycle = 0
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
    }

    private fun scheduleAlarm(context: Context, timeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = "nz.ac.uclive.jis48.timescribe.ALARM_ACTION"
        val flags = PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 (API 31)
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                handleExactAlarmPermission(context, timeInMillis)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
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
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun getProgress(): Float {
        val totalDuration = when (if (timerState.value == TimerState.IDLE && timeElapsedState != 0) lastNonIdleState else timerState.value) {
            TimerState.WORK -> settings.value.workDuration * 60
            TimerState.BREAK -> settings.value.breakDuration * 60
            TimerState.LONG_BREAK -> settings.value.longBreakDuration * 60
            else -> 0
        }
        return if (totalDuration > 0) timeElapsed.value.toFloat() / totalDuration else 0f
    }

    fun getCurrentStateDuration(): String {
        return when (if (timerState.value == TimerState.IDLE && timeElapsedState != 0) lastNonIdleState else timerState.value) {
            TimerState.WORK -> "Work: ${settings.value.workDuration} mins"
            TimerState.BREAK -> "Break: ${settings.value.breakDuration} mins"
            TimerState.LONG_BREAK -> "Long Break: ${settings.value.longBreakDuration} mins"
            else -> "Work: ${settings.value.workDuration} mins"
        }
    }

    fun getCurrentWorkDuration(): Int {
        return settings.value.workDuration
    }

    fun getCurrentCycle(): Int {
        return currentCycle
    }

    fun continueToNextState() {
        startTime = System.currentTimeMillis()
        timerState.value = nextState ?: TimerState.IDLE
        nextState = null
    }

    private fun handleExactAlarmPermission(context: Context, timeInMillis: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(context)
                    .setTitle("Permission Required")
                    .setMessage("To ensure that your timer works accurately, TimeScribe needs permission to schedule exact alarms. Please grant this permission in the app settings.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        // Open app settings
                        val intent = Intent().apply {
                            action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                            data = android.net.Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                scheduleExactAlarm(context, timeInMillis)
            }
        } else {
            scheduleExactAlarm(context, timeInMillis)
        }
    }

    private fun scheduleExactAlarm(context: Context, timeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }
}

