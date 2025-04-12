package com.example.locationinfoapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_locations")
data class SavedLocation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val thumbnailPath: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val address: String? = null
)