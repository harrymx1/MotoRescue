package com.modulpapb.motorescue.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.modulpapb.motorescue.model.listMontirPalsu

@Composable
fun HomeScreen(
    onSosClick: () -> Unit // Callback saat tombol SOS ditekan
) {
    // Lokasi Default (Monas)
    val monasLocation = LatLng(-6.175392, 106.827153)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(monasLocation, 16f)
    }

    // Izin Lokasi
    val context = LocalContext.current
    var isLocationPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!isLocationPermissionGranted) {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            // 1. PETA GOOGLE
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = isLocationPermissionGranted),
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                // Tampilkan Marker untuk setiap Montir Palsu
                listMontirPalsu.forEach { montir ->
                    Marker(
                        state = MarkerState(position = montir.lokasi),
                        title = montir.nama,
                        snippet = "Spesialis: ${montir.keahlian}",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE) // Warna Oranye biar beda
                    )
                }
            }

            // 2. TOMBOL SOS (MOGOK)
            Button(
                onClick = { onSosClick() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Posisi di bawah tengah
                    .padding(bottom = 32.dp)
                    .size(width = 200.dp, height = 60.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                Text(text = "SAYA MOGOK!", color = Color.White, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}