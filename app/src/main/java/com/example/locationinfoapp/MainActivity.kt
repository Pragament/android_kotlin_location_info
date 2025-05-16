package com.example.locationinfoapp

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.locationinfoapp.data.AppDatabase
import com.example.locationinfoapp.model.PincodeLocation
import com.example.locationinfoapp.ui.theme.LocationInfoAppTheme
import com.example.locationinfoapp.viewmodel.LocationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // Permissions granted
        }
    }

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "locations.db"
        ).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        setContent {
            LocationInfoAppTheme {
                AppContent(database = database)
            }
        }
    }
}

@Composable
private fun AppContent(database: AppDatabase) {
    val context = LocalContext.current
    var showHistory by remember { mutableStateOf(false) }
    val viewModel: LocationViewModel = viewModel(
        factory = LocationViewModel.provideFactory(context, database.locationDao())
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (showHistory) {
                HistoryScreen(
                    viewModel = viewModel,
                    onBack = { showHistory = false },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LocationScreen(
                    viewModel = viewModel,
                    onShowHistory = { showHistory = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val currentLat = 28.6139
                        val currentLon = 77.2090
                        val allPincodes = readPincodeData(context)
                        val nearby = allPincodes.filter { location ->
                            distanceBetween(
                                currentLat,
                                currentLon,
                                location.latitude,
                                location.longitude
                            ) <= 10.0
                        }.map { it.pincode }
                        println("Nearby pincodes: $nearby")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Find Nearby Pincodes")
            }
        }
    }
}