package com.example.locationinfoapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import com.example.locationinfoapp.model.PincodeLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(location: PincodeLocation)

    @Query("SELECT * FROM pincode_locations")
    fun getAllLocations(): Flow<List<PincodeLocation>>

    @Delete
    suspend fun delete(location: PincodeLocation)
}