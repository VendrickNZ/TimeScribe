package nz.ac.uclive.jis48.timescribe.data

import java.util.Date

data class TagInterval(
    val tag: Tag,
    val startDate: Date,
    val endDate: Date
)
