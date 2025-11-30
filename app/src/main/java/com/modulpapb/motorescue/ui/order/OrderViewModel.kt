package com.modulpapb.motorescue.ui.order

import android.app.Application
import android.net.Uri
import android.util.Log // Tambahan untuk Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.modulpapb.motorescue.utils.bitmapToBase64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val context = application.applicationContext

    private val _orderState = MutableStateFlow<OrderState>(OrderState.Idle)
    val orderState: StateFlow<OrderState> = _orderState

    fun submitOrder(masalah: String, photoUri: Uri?) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            Log.d("DEBUG_ORDER", "1. Fungsi submitOrder dipanggil. Masalah: $masalah") // CCTV 1

            try {
                val userId = auth.currentUser?.uid ?: "anonymous"
                Log.d("DEBUG_ORDER", "2. User ID: $userId") // CCTV 2

                var photoBase64 = ""

                // 1. UBAH FOTO JADI TEKS
                if (photoUri != null) {
                    Log.d("DEBUG_ORDER", "3. Mulai konversi foto...") // CCTV 3
                    photoBase64 = bitmapToBase64(photoUri, context)
                    Log.d("DEBUG_ORDER", "4. Konversi selesai. Panjang string: ${photoBase64.length}") // CCTV 4
                } else {
                    Log.d("DEBUG_ORDER", "3. Tidak ada foto yang dipilih")
                }

                // 2. SIMPAN KE FIRESTORE
                val orderData = hashMapOf(
                    "userId" to userId,
                    "masalah" to masalah,
                    "photoBase64" to photoBase64,
                    "status" to "Mencari Montir",
                    "timestamp" to System.currentTimeMillis()
                )

                Log.d("DEBUG_ORDER", "5. Mencoba kirim ke Firestore...") // CCTV 5

                // Perintah kirim
                val docRef = firestore.collection("orders").add(orderData).await()

                Log.d("DEBUG_ORDER", "6. SUKSES! Data masuk dengan ID: ${docRef.id}") // CCTV 6

                _orderState.value = OrderState.Success

            } catch (e: Exception) {
                // Tangkap errornya dan tampilkan di Logcat
                Log.e("DEBUG_ORDER", "ERROR FATAL: ${e.message}", e) // CCTV ERROR
                _orderState.value = OrderState.Error(e.message ?: "Gagal kirim order")
            }
        }
    }

    fun resetState() {
        _orderState.value = OrderState.Idle
    }
}

sealed class OrderState {
    object Idle : OrderState()
    object Loading : OrderState()
    object Success : OrderState()
    data class Error(val message: String) : OrderState()
}