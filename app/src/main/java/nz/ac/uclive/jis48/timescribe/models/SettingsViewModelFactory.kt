package nz.ac.uclive.jis48.timescribe.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nz.ac.uclive.jis48.timescribe.data.SettingsRepository

class SettingsViewModelFactory(private val repository: SettingsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
