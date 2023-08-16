package nz.ac.uclive.jis48.timescribe.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import nz.ac.uclive.jis48.timescribe.data.TimerRepository

class TimerViewModelFactory(
    private val context: Context,
    private val settingsViewModel: SettingsViewModel
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            val timerRepository = TimerRepository(context)
            return TimerViewModel(settingsViewModel, timerRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
