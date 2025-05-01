package nz.ac.uclive.jis48.timescribe.data

data class Tag(
    val id: Int, // a unique identifier
    val name: String, // the name of the tag e.g., work, or break, or long break
    val color: Int // the associated colour
)
