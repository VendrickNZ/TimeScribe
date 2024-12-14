package nz.ac.uclive.jis48.timescribe.data

import java.util.Date

data class Session(
    val startDate: Date,
    val endDate: Date,
    val totalPauseDuration: Long,
    val totalWorkDuration: Long,
    val pauseIntervals: List<Pair<Date, Date>> = listOf(),
    val tagInterval: List<TagInterval> = emptyList()
)
