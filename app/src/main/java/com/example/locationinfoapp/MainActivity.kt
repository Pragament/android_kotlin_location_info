package com.example.locationinfoapp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.example.locationinfoapp.ui.theme.LocationInfoAppTheme
import androidx.room.Room

class MainActivity : ComponentActivity() {
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION,
                false) -> {
                // Precise location access granted
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION,
                false) -> {
                // Approximate location access granted
            }
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

        locationPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        setContent {
            LocationInfoAppTheme {
                AppContent(database = database)
            }
        }
    }

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }
}

@Composable
private fun AppContent(database: AppDatabase) {
    val context = LocalContext.current
    var showHistory by remember { mutableStateOf(false) }
    val viewModel = remember { LocationViewModel(context, database.locationDao()) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (showHistory) {
            HistoryScreen(
                viewModel = viewModel,
                onBack = { showHistory = false },
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LocationScreen(
                viewModel = viewModel,
                onShowHistory = { showHistory = true },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}


