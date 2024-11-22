package nz.ac.uclive.jis48.timescribe.data

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import nz.ac.uclive.jis48.timescribe.MainActivity
import nz.ac.uclive.jis48.timescribe.R

const val NOTIFICATION_ID = 1
const val CHANNEL_ID = "timescribe_channel"
const val ACTION_NOTIFY_TIME_IS_OVER = "nz.ac.uclive.jis48.timescribe.action.NOTIFY_TIME_IS_OVER"


class TimerService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = if (intent?.action == ACTION_NOTIFY_TIME_IS_OVER) {
            notifyTimeIsOver()
            createTimerFinishedNotification()
        } else {
            createNotification()
        }

        startForeground(NOTIFICATION_ID, notification)

        if (intent?.action != ACTION_NOTIFY_TIME_IS_OVER) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "TimerService::WakeLock"
            )
            wakeLock?.acquire(60 * 60 * 1000L /* 60 minutes */)
        }

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Timer Running")
            .setContentText("Your timer is running in the background")
            .setPriority(NotificationCompat.PRIORITY_LOW)
        return builder.build()
    }


    private fun createTimerFinishedNotification(): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Time's Up!")
            .setContentText("Your work/break time is over.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
        return builder.build()
    }

    private fun notifyTimeIsOver() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createTimerFinishedNotification())

        // a lot of deprecated methods round these parts
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        vib.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
    }




    override fun onDestroy() {
        // TODO: Stop your timer logic here
        wakeLock?.release()
        wakeLock = null
        super.onDestroy()
    }
}
