package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_wheel_lists")
data class CustomWheelListEntity(
    @PrimaryKey val id: String,
    val name: String,
    val emoji: String,
    val description: String = "",
    val movieIdsCsv: String = "",
    val customMoviesJson: String = "[]",
    val accentColorHex: Long = 0xFF8B5CF6,
    val isPreset: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
