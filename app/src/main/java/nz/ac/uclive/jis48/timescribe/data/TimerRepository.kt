package nz.ac.uclive.jis48.timescribe.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class TimerRepository(private val context: Context) {

    fun saveSession(session: Session) {
        val sessions = loadSessions().toMutableList()
        sessions.add(session)
        val gson = Gson()
        val jsonString = gson.toJson(sessions)
        Log.d("Saving JSON String", jsonString)
        context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
            it.write(jsonString.toByteArray())
        }
    }

    fun loadSessions(): List<Session> {
        return try {
            context.openFileInput(FILE_NAME).use {
                val jsonString = it.bufferedReader().readText()
                if (jsonString.isBlank() || jsonString == "[]") return emptyList()

                Log.d("JSON String", jsonString)
                val gson = Gson()
                val typeToken = object : TypeToken<List<Session>>() {}.type
                gson.fromJson(jsonString, typeToken) ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun loadTodaySessions(): List<Session> {
        val allSessions = loadSessions()
        return allSessions.filter {
            it.startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == LocalDate.now()
        }
    }

    fun loadSessionsForDate(selectedDate: Date): List<Session> {
        val allSessions = loadSessions()
        return allSessions.filter {
            val sessionDate = it.startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            val targetDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            sessionDate == targetDate
        }
    }


    companion object {
        private const val FILE_NAME = "timers.json"
    }
}
