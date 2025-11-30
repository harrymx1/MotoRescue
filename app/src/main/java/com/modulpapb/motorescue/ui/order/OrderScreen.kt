package com.modulpapb.motorescue.ui.order

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.modulpapb.motorescue.utils.createFile
import java.util.concurrent.Executors

@Composable
fun OrderScreen(
    onOrderSubmitted: () -> Unit // Callback saat tombol kirim ditekan
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State untuk data Form
    var problemDescription by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // State untuk mode Kamera (Apakah sedang buka kamera atau tidak)
    var isCameraOpen by remember { mutableStateOf(false) }

    // State untuk Izin Kamera
    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasCameraPermission = it
    }

    // Persiapan CameraX
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // TAMPILAN 1: MODE KAMERA AKTIF
    if (isCameraOpen) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Viewfinder Kamera (CameraX)
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val provider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner, cameraSelector, preview, imageCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Tombol Jepret
            Button(
                onClick = {
                    val photoFile = createFile(context)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val savedUri = Uri.fromFile(photoFile)
                            photoUri = savedUri // Simpan hasil foto
                            isCameraOpen = false // Tutup kamera
                        }

                        override fun onError(exc: ImageCaptureException) {
                            Toast.makeText(context, "Gagal ambil foto: ${exc.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .size(70.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                // Icon tombol jepret
            }
        }
    }

    // TAMPILAN 2: MODE FORMULIR
    else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Formulir Bantuan", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            // Input Masalah
            OutlinedTextField(
                value = problemDescription,
                onValueChange = { problemDescription = it },
                label = { Text("Jelaskan Masalah Motor Anda") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preview Foto (Jika sudah ada foto)
            if (photoUri != null) {
                Text("Bukti Foto Terlampir:")
                Image(
                    painter = rememberAsyncImagePainter(photoUri),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(8.dp)
                        .background(Color.Gray)
                )
            } else {
                // Tombol Buka Kamera
                OutlinedButton(
                    onClick = {
                        // Cek izin dulu
                        if (hasCameraPermission) {
                            isCameraOpen = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ambil Foto Kerusakan")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Tombol Kirim
            Button(
                onClick = {
                    if (problemDescription.isNotEmpty()) {
                        onOrderSubmitted() // Pindah ke layar selanjutnya
                    } else {
                        Toast.makeText(context, "Mohon isi deskripsi masalah", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = problemDescription.isNotEmpty()
            ) {
                Text("CARI BENGKEL TERDEKAT")
            }
        }
    }
}