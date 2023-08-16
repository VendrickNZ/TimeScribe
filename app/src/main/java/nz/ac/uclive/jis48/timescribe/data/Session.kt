package nz.ac.uclive.jis48.timescribe.data

import java.util.Date

data class Session(
    val startDate: Date,
    val endDate: Date,
    val pauseCount: Int,
    val totalPauseDuration: Long,
    val pauseIntervals: List<Pair<Date, Date>> = listOf()
)
