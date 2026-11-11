package com.arnaucaballer.fitia.feature.training.ui

data class MuscleZone(
    val id: String,
    val title: String,
    val generalName: String,
    val front: Boolean,
    val bodyHits: List<MuscleSpot>,
    val cropLeft: Float,
    val cropTop: Float,
    val cropRight: Float,
    val cropBottom: Float,
    val parts: List<MuscleSpot>,
)
