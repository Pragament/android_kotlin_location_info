package com.example.locationinfoapp

import android.util.Log
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    viewModel: LocationViewModel,
    onShowHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLocation by viewModel.currentLocation.collectAsState()
    val latitudeText by viewModel.latitude.collectAsState()
    val longitudeText by viewModel.longitude.collectAsState()
    val isMapMoving by viewModel.isMapMoving.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    fun shouldUpdateMap(newPoint: GeoPoint, currentCenter: GeoPoint): Boolean {
        return newPoint.distanceToAsDouble(currentCenter) > 10 // 10 meter threshold
    }

    var mapView by remember { mutableStateOf<MapView?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Location Finder") }
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = {
                        Toast.makeText(context, "Saved Location!",
                            Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            viewModel.saveCurrentLocation(mapViewRef)
                        }
                    }
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save location")
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(
                    onClick = onShowHistory
                ) {
                    Icon(Icons.Default.History, contentDescription = "View history")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapViewRef = this
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        minZoomLevel = 3.0
                        maxZoomLevel = 19.0
                        controller.setZoom(15.0)

                        addMapListener(object : MapListener {
                            override fun onScroll(event: ScrollEvent?): Boolean {
                                if (!viewModel.isManualUpdate.value) {
                                    viewModel.setMapMoving(true)
                                    event?.source?.mapCenter?.let { center ->
                                        viewModel.updateMapCenter(GeoPoint(center.latitude,
                                            center.longitude))
                                    }
                                }
                                return true
                            }

                            override fun onZoom(event: ZoomEvent?): Boolean {
                                if (!viewModel.isManualUpdate.value) {
                                    viewModel.setMapMoving(true)
                                    event?.source?.mapCenter?.let { center ->
                                        viewModel.updateMapCenter(GeoPoint(center.latitude,
                                            center.longitude))
                                    }
                                }
                                return true
                            }
                        })

                        viewTreeObserver.addOnGlobalLayoutListener(
                            object : ViewTreeObserver.OnGlobalLayoutListener {
                                override fun onGlobalLayout() {
                                    if (!isMapMoving) {
                                        val center = GeoPoint(mapCenter.latitude,
                                            mapCenter.longitude)
                                        viewModel.onMapIdle(center)
                                    }
                                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                                }
                            }
                        )
                    }.also { mapView = it }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(-1f),
                update = { mapView ->
                    currentLocation?.let { geoPoint ->
                        // Verify the received coordinates
                        if (abs(geoPoint.latitude - mapView.mapCenter.latitude) > 0.00001 ||
                            abs(geoPoint.longitude - mapView.mapCenter.longitude) > 0.00001) {

                            mapView.controller.setCenter(geoPoint)
                            mapView.overlays.clear()
                            Marker(mapView).apply {
                                position = geoPoint
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Target Location"
                                snippet = "${geoPoint.latitude}, ${geoPoint.longitude}"
                                mapView.overlays.add(this)
                            }
                            mapView.invalidate() // Force redraw
                        }
                    }
                }
            )

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = latitudeText,
                                onValueChange = { newLat ->
                                    viewModel.updateCoordinates(newLat, longitudeText)
                                },
                                label = { Text("Latitude") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                )
                            )
                            OutlinedTextField(
                                value = longitudeText,
                                onValueChange = { newLon ->
                                    viewModel.updateCoordinates(latitudeText, newLon)
                                },
                                label = { Text("Longitude") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (viewModel.hasLocationPermission()) {
                                        viewModel.getRealCurrentLocation()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Current Location")
                            }
                            Button(onClick = {
                                Log.d("LocationInput",
                                    "Raw input - Lat: ${viewModel.latitude.value}, " +
                                            "Lon: ${viewModel.longitude.value}")
                                viewModel.goToSpecifiedLocation()
                            }) {
                                Text("Go to Location")
                            }
                        }
                    }
                }

                if (isMapMoving) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Uncomment to show loading indicator
                        // CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
