package nz.ac.uclive.jis48.timescribe.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val dataStore: DataStore<Preferences> = context.dataStore
    companion object {
        val WORK_DURATION_KEY = intPreferencesKey("work_duration")
        val BREAK_DURATION_KEY = intPreferencesKey("break_duration")
        val CYCLES_BEFORE_LONG_BREAK_KEY = intPreferencesKey("cycles_before_long_break")
        val LONG_BREAK_DURATION_KEY = intPreferencesKey("long_break_duration")
        val SOUND_NOTIFICATION_KEY = booleanPreferencesKey("sound_notification")
        val POPUP_NOTIFICATION_KEY = booleanPreferencesKey("popup_notification")
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

    }

    suspend fun saveSettings(settings: Settings) {
        dataStore.edit { preferences ->
            preferences[WORK_DURATION_KEY] = settings.workDuration
            preferences[BREAK_DURATION_KEY] = settings.breakDuration
            preferences[LONG_BREAK_DURATION_KEY] = settings.longBreakDuration
            preferences[CYCLES_BEFORE_LONG_BREAK_KEY] = settings.cyclesBeforeLongBreak
            preferences[SOUND_NOTIFICATION_KEY] = settings.soundNotification
            preferences[POPUP_NOTIFICATION_KEY] = settings.popupNotification
            preferences[DARK_MODE_KEY] = settings.darkMode
        }
    }


    val settingsFlow: Flow<Settings> = dataStore.data
        .map { preferences ->
            Settings(
                workDuration = preferences[WORK_DURATION_KEY] ?: 25,
                breakDuration = preferences[BREAK_DURATION_KEY] ?: 5,
                longBreakDuration = preferences[LONG_BREAK_DURATION_KEY] ?: 15,
                cyclesBeforeLongBreak = preferences[CYCLES_BEFORE_LONG_BREAK_KEY] ?: 4,
                soundNotification = preferences[SOUND_NOTIFICATION_KEY] ?: true,
                popupNotification = preferences[POPUP_NOTIFICATION_KEY] ?: true,
                darkMode = preferences[DARK_MODE_KEY] ?: false
            )
        }
}
