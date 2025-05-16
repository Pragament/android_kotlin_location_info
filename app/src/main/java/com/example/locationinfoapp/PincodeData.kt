package com.example.locationinfoapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pincode_locations")
data class PincodeLocation(
    @PrimaryKey val pincode: String,
    val latitude: Double,
    val longitude: Double
)