package dto

data class TodoDto(
    val id: Long = 0,
    val text: String = "0",
    val completed: Boolean? = true
)
