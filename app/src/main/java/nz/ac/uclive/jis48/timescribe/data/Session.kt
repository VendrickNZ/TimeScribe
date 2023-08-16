package nz.ac.uclive.jis48.timescribe.data

import java.util.Date

data class Session(
    val startDate: Date,
    val startTime: String,
    val pauseCount: Int,
    val totalPauseDuration: Long,
    val endTime: String,
    val totalTime: Long
)
