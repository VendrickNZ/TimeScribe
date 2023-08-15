package nz.ac.uclive.jis48.timescribe.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nz.ac.uclive.jis48.timescribe.data.Settings
import nz.ac.uclive.jis48.timescribe.data.SettingsRepository

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val settingsFlow = repository.settingsFlow

    fun saveSettings(settings: Settings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
        }
    }
}
