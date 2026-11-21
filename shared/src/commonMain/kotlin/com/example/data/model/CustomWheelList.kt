package com.example.data.model

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class CustomWheelList(
    val id: String,
    val name: String,
    val emoji: String = "🎬",
    val description: String = "",
    val movies: List<Movie>,
    val accentColorHex: Long = 0xFF8B5CF6,
    val isPreset: Boolean = false,
    val updatedAtMillis: Long = Clock.System.now().toEpochMilliseconds()
)
