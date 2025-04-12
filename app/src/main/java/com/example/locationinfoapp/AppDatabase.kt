package com.example.locationinfoapp

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SavedLocation::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
}