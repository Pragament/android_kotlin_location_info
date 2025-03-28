package com.example.locationinfoapp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.example.locationinfoapp.ui.theme.LocationInfoAppTheme

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        setContent {
            LocationInfoAppTheme {
                val context = LocalContext.current
                val viewModel = remember { LocationViewModel(context) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LocationScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        requestLocationPermissions()
    }

    private fun requestLocationPermissions() {
        locationPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }
}


