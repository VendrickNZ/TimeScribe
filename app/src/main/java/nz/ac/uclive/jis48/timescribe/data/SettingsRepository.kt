package nz.ac.uclive.jis48.timescribe.data

import android.content.Context
import android.util.Log
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
        val AUTO_SAVE_KEY = booleanPreferencesKey("auto_save")
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")

    }

    suspend fun saveSettings(settings: Settings) {
        Log.d("SettingsRepository", "Saving settings in repo: $settings")
        dataStore.edit { preferences ->
            preferences[WORK_DURATION_KEY] = settings.workDuration
            preferences[BREAK_DURATION_KEY] = settings.breakDuration
            preferences[LONG_BREAK_DURATION_KEY] = settings.longBreakDuration
            preferences[CYCLES_BEFORE_LONG_BREAK_KEY] = settings.cyclesBeforeLongBreak
            preferences[AUTO_SAVE_KEY] = settings.autoSave
            preferences[DARK_MODE_KEY] = settings.darkMode
        }
        Log.d("SettingsRepository", "Settings saved successfully")
    }


    val settingsFlow: Flow<Settings> = dataStore.data
        .map { preferences ->
            Log.d("SettingsRepository", "Loading settings from repo: $preferences")
            Log.d(WORK_DURATION_KEY.toString(), preferences[WORK_DURATION_KEY].toString())
            Log.d(BREAK_DURATION_KEY.toString(), preferences[BREAK_DURATION_KEY].toString())
            Log.d(LONG_BREAK_DURATION_KEY.toString(), preferences[LONG_BREAK_DURATION_KEY].toString())
            Log.d(CYCLES_BEFORE_LONG_BREAK_KEY.toString(), preferences[CYCLES_BEFORE_LONG_BREAK_KEY].toString())
            Log.d(AUTO_SAVE_KEY.toString(), preferences[AUTO_SAVE_KEY].toString())
            Log.d(DARK_MODE_KEY.toString(), preferences[DARK_MODE_KEY].toString())
            Settings(
                workDuration = preferences[WORK_DURATION_KEY] ?: 25,
                breakDuration = preferences[BREAK_DURATION_KEY] ?: 5,
                longBreakDuration = preferences[LONG_BREAK_DURATION_KEY] ?: 15,
                cyclesBeforeLongBreak = preferences[CYCLES_BEFORE_LONG_BREAK_KEY] ?: 4,
                autoSave = preferences[AUTO_SAVE_KEY] ?: false,
                darkMode = preferences[DARK_MODE_KEY] ?: false
            )
        }
}
