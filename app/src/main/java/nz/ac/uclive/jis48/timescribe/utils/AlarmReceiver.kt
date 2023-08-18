package nz.ac.uclive.jis48.timescribe.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import nz.ac.uclive.jis48.timescribe.data.ACTION_NOTIFY_TIME_IS_OVER
import nz.ac.uclive.jis48.timescribe.data.TimerService

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, TimerService::class.java)
        serviceIntent.action = ACTION_NOTIFY_TIME_IS_OVER
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
