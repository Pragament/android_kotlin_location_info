package com.example.locationinfoapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.locationinfoapp.data.LocationDao

class LocationViewModel(
    private val context: Context,
    private val locationDao: LocationDao
) : ViewModel() {
    companion object {
        fun provideFactory(
            context: Context,
            locationDao: LocationDao
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LocationViewModel(context, locationDao) as T
            }
        }
    }
}