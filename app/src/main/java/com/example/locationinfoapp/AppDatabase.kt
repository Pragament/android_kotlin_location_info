package com.example.locationinfoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.locationinfoapp.model.PincodeLocation

@Database(
    entities = [PincodeLocation::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}