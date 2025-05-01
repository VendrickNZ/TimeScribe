package nz.ac.uclive.jis48.timescribe.data

import java.util.Date

data class TagInterval(
    val tag: Tag, // the tags information
    val startDate: Date, // start time of specific tag
    val endDate: Date // end time of specific tag
)
