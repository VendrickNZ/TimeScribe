package nz.ac.uclive.jis48.timescribe.data

data class Settings(
    val workDuration: Int = 25,
    val breakDuration: Int = 5,
    val longBreakDuration: Int = 15,
    val cyclesBeforeLongBreak: Int = 4,
    val soundNotification: Boolean = true,
    val popupNotification: Boolean = true,
    val darkMode: Boolean = false
)
