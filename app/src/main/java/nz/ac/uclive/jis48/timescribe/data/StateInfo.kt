package nz.ac.uclive.jis48.timescribe.data

import nz.ac.uclive.jis48.timescribe.models.TimerViewModel.TimerState

data class StateInfo(
    var currentState: TimerState = TimerState.IDLE,
    var previousState: TimerState = TimerState.IDLE,
    var currentStateName: String = "Idle",
    var previousStateName: String = "Idle",
    var duration: Int = 0
)