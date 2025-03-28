package com.example.locationinfoapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.util.GeoPoint

class LocationViewModel(
    private val context: Context
) : ViewModel() {
    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation: StateFlow<GeoPoint?> = _currentLocation.asStateFlow()

    var latitude = ""
    var longitude = ""

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val locationRequest: LocationRequest by lazy {
        LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    fun getRealCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    _currentLocation.value = GeoPoint(it.latitude,
                        it.longitude)
                    latitude = it.latitude.toString()
                    longitude = it.longitude.toString()
                }
            }.addOnFailureListener { e ->
                // Handle error
                Log.e("Location", "Error getting location", e)
            }
        }
    }


    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                _currentLocation.value = GeoPoint(location.latitude,
                    location.longitude)
            }
        }
    }

    fun updateCoordinates(lat: String, lon: String) {
        latitude = lat
        longitude = lon
    }

    fun startLocationUpdates() {
        if (hasLocationPermission()) {
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                )
            } catch (e: SecurityException) {
                // Handle permission exception
            }
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun getCurrentLocation() {
        if (hasLocationPermission()) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        _currentLocation.value = GeoPoint(it.latitude,
                            it.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // Handle permission exception
            }
        }
    }

    fun goToSpecifiedLocation() {
        try {
            _currentLocation.value = GeoPoint(
                latitude.toDouble(),
                longitude.toDouble()
            )
        } catch (e: NumberFormatException) {
            // Handle invalid coordinates
        }
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}