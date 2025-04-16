package com.example.locationinfoapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.File
import java.io.FileOutputStream

class LocationViewModel(
    private val context: Context,
    private val locationDao: LocationDao
) : ViewModel() {
    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation: StateFlow<GeoPoint?> = _currentLocation.asStateFlow()

    private val _latitude = MutableStateFlow("")
    val latitude: StateFlow<String> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow("")
    val longitude: StateFlow<String> = _longitude.asStateFlow()

    private val _mapCenter = MutableStateFlow<GeoPoint?>(null)
    val mapCenter: StateFlow<GeoPoint?> = _mapCenter.asStateFlow()

    private val _isMapMoving = MutableStateFlow(false)
    val isMapMoving: StateFlow<Boolean> = _isMapMoving.asStateFlow()

    private val _isManualUpdate = MutableStateFlow(false)
    val isManualUpdate: StateFlow<Boolean> = _isManualUpdate.asStateFlow()

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
                    _currentLocation.value = GeoPoint(it.latitude, it.longitude)
                    _latitude.value = "%.6f".format(it.latitude)
                    _longitude.value = "%.6f".format(it.longitude)
                }
            }.addOnFailureListener { e ->
                Log.e("Location", "Error getting location", e)
            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                _currentLocation.value = GeoPoint(location.latitude,
                    location.longitude)
                _latitude.value = location.latitude.toString()
                _longitude.value = location.longitude.toString()
            }
        }
    }

    fun updateCoordinates(lat: String, lon: String) {
        _latitude.value = lat
        _longitude.value = lon
    }

    fun setMapMoving(isMoving: Boolean) {
        _isMapMoving.value = isMoving
        if (!isMoving && _mapCenter.value != null) {
            updateFromMapCenter(_mapCenter.value!!)
        }
    }

    fun updateMapCenter(point: GeoPoint) {
        _mapCenter.value = point
        // Optionally, you can update the latitude and longitude here for real-time updates during movement:
        _latitude.value = "%.6f".format(point.latitude)
        _longitude.value = "%.6f".format(point.longitude)
    }

    private fun updateFromMapCenter(point: GeoPoint) {
        _currentLocation.value = point
        _latitude.value = "%.6f".format(point.latitude)
        _longitude.value = "%.6f".format(point.longitude)
    }

    fun onMapIdle(center: GeoPoint) {
        _isMapMoving.value = false
        updateFromMapCenter(center)
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

    private fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        return lat in -90.0..90.0 &&
                lon in -180.0..180.0 &&
                !lat.isNaN() &&
                !lon.isNaN()
    }

    fun goToSpecifiedLocation() {
        try {
            // Clean and validate input
            val cleanLat = latitude.value
                .replace(',', '.')  // Handle both decimal separators
                .trim()
            val cleanLon = longitude.value
                .replace(',', '.')
                .trim()

            if (cleanLat.isBlank() || cleanLon.isBlank()) return

            val lat = cleanLat.toDouble()
            val lon = cleanLon.toDouble()

            // Validate coordinate ranges
            if (lat !in -90.0..90.0 || lon !in -180.0..180.0) {
                Log.e("Location", "Coordinates out of range: ($lat, $lon)")
                return
            }

            // Create and verify GeoPoint
            val newLocation = GeoPoint(lat, lon)
            if (newLocation.latitude != lat || newLocation.longitude != lon) {
                Log.e("Location", "GeoPoint conversion failed")
                return
            }

            _currentLocation.value = newLocation
            _mapCenter.value = newLocation
            Log.d("Location", "Going to: ($lat, $lon)")

        } catch (e: Exception) {
            Log.e("Location", "Failed to parse coordinates", e)
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

    suspend fun saveCurrentLocation(mapView: MapView?, address: String? = null) {
        currentLocation.value?.let { point ->
            val thumbnailPath = captureMapThumbnail(point, mapView)
            val location = SavedLocation(
                latitude = point.latitude,
                longitude = point.longitude,
                thumbnailPath = thumbnailPath,
                address = address
            )
            locationDao.insert(location)
        }
    }

    fun getAllSavedLocations(): Flow<List<SavedLocation>> {
        return locationDao.getAllLocations()
    }

    suspend fun deleteLocation(location: SavedLocation) {
        locationDao.delete(location)
        // Delete thumbnail file if exists
        location.thumbnailPath?.let { path ->
            File(path).delete()
        }
    }

    private suspend fun captureMapThumbnail(point: GeoPoint, mapView: MapView?): String? {
        return try {
            val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
            mapView?.draw(Canvas(bitmap))
            val file = File(context.cacheDir, "thumb_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("Thumbnail", "Failed to capture thumbnail", e)
            null
        }
    }
}