package com.modulpapb.motorescue.ui.order

import android.Manifest
import android.net.Uri
import android.util.Log // Import untuk Logging
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
import androidx.compose.material.icons.filled.Add
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.modulpapb.motorescue.utils.createFile
import java.util.concurrent.Executors

@Composable
fun OrderScreen(
    onOrderSubmitted: () -> Unit,
    // INI PENTING: Variabel viewModel dideklarasikan di sini agar dikenal di bawah
    viewModel: OrderViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State UI
    var problemDescription by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var isCameraOpen by remember { mutableStateOf(false) }

    // Pantau Status Upload
    val orderState by viewModel.orderState.collectAsState()

    // Efek Samping: Kalau Sukses / Error
    LaunchedEffect(orderState) {
        when (orderState) {
            is OrderState.Success -> {
                Log.d("DEBUG_ORDER", "State Sukses Diterima UI. Pindah Layar.")
                Toast.makeText(context, "Order Terkirim! Mencari Montir...", Toast.LENGTH_LONG).show()
                viewModel.resetState()
                onOrderSubmitted() // Keluar dari halaman ini
            }
            is OrderState.Error -> {
                val msg = (orderState as OrderState.Error).message
                Log.e("DEBUG_ORDER", "State Error Diterima UI: $msg")
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // State Izin Kamera
    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasCameraPermission = it
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    // --- TAMPILAN KAMERA ---
    if (isCameraOpen) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                        provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                    } catch (e: Exception) { e.printStackTrace() }
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
            Button(
                onClick = {
                    val photoFile = createFile(context)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            photoUri = Uri.fromFile(photoFile)
                            isCameraOpen = false
                        }
                        override fun onError(exc: ImageCaptureException) {
                            Toast.makeText(context, "Gagal: ${exc.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp).size(70.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {}
        }
    }
    // --- TAMPILAN FORMULIR ---
    else {
        // Jika sedang Loading (Upload), kunci layar dengan Loading Spinner
        if (orderState is OrderState.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
                Text("Mengirim Data...", modifier = Modifier.padding(top = 64.dp))
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Formulir Bantuan", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = problemDescription,
                    onValueChange = { problemDescription = it },
                    label = { Text("Jelaskan Masalah Motor Anda") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (photoUri != null) {
                    Text("Bukti Foto:")
                    Image(
                        painter = rememberAsyncImagePainter(photoUri),
                        contentDescription = null,
                        modifier = Modifier.size(200.dp).padding(8.dp).background(Color.Gray)
                    )
                } else {
                    OutlinedButton(
                        onClick = {
                            if (hasCameraPermission) isCameraOpen = true
                            else permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ambil Foto Kerusakan")
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // TOMBOL KIRIM (DENGAN LOGGING)
                Button(
                    onClick = {
                        // LOG 1: Cek apakah tombol merespon
                        Log.d("DEBUG_ORDER", "Tombol Ditekan! Masalah: '$problemDescription'")

                        if (problemDescription.isNotEmpty()) {
                            // LOG 2: Validasi lolos
                            Log.d("DEBUG_ORDER", "Validasi Lolos. Memanggil ViewModel...")
                            viewModel.submitOrder(problemDescription, photoUri)
                        } else {
                            // LOG 3: Validasi gagal
                            Log.d("DEBUG_ORDER", "Validasi Gagal: Deskripsi Kosong")
                            Toast.makeText(context, "Isi deskripsi dulu", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("CARI BENGKEL TERDEKAT")
                }
            }
        }
    }
}