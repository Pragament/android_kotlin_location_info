package com.example.locationinfoapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    viewModel: LocationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLocation by viewModel.currentLocation.collectAsState()

    // Local state for text fields
    var latitudeText by remember { mutableStateOf(viewModel.latitude) }
    var longitudeText by remember { mutableStateOf(viewModel.longitude) }

    // Update ViewModel when text changes
    LaunchedEffect(latitudeText, longitudeText) {
        viewModel.updateCoordinates(latitudeText, longitudeText)
    }

    // Update local state when ViewModel changes
    LaunchedEffect(viewModel.latitude, viewModel.longitude) {
        latitudeText = viewModel.latitude
        longitudeText = viewModel.longitude
    }

    var mapView by remember { mutableStateOf<MapView?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Location Finder") }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Map View
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        minZoomLevel = 3.0
                        maxZoomLevel = 19.0
                        controller.setZoom(15.0)
                        mapView = this
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(-1f)
            ) { view ->
                currentLocation?.let { geoPoint ->
                    view.overlays.clear()
                    Marker(view).apply {
                        position = geoPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Selected Location"
                        view.overlays.add(this)
                    }
                    view.controller.setCenter(geoPoint)
                }
            }

            // Controls Column
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
                                onValueChange = { latitudeText = it },
                                label = { Text("Latitude") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                )
                            )

                            OutlinedTextField(
                                value = longitudeText,
                                onValueChange = { longitudeText = it },
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
                                    } else {
                                        // Show rationale or request permissions again
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Current Location")
                            }

                            Button(
                                onClick = { viewModel.goToSpecifiedLocation() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Go to Location")
                            }
                        }
                    }
                }
            }
        }
    }
}