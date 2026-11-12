package app.fitia.feature.nutrition.domain

data class WaterLog(
    val id: Long = 0,
    val dayEpochDay: Long,
    val amountMl: Int,
    val createdAtEpochMs: Long,
)
