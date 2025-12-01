package com.modulpapb.motorescue.ui.order

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.modulpapb.motorescue.data.AppDatabase
import com.modulpapb.motorescue.data.OrderHistory
import com.modulpapb.motorescue.utils.bitmapToBase64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class OrderViewModel(application: Application) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val context = application.applicationContext

    // Inisialisasi Database Lokal
    private val database = AppDatabase.getDatabase(context)
    private val dao = database.orderDao()

    private val _orderState = MutableStateFlow<OrderState>(OrderState.Idle)
    val orderState: StateFlow<OrderState> = _orderState

    fun submitOrder(masalah: String, photoUri: Uri?) {
        viewModelScope.launch {
            _orderState.value = OrderState.Loading
            Log.d("DEBUG_ORDER", "1. Submit dimulai: $masalah")

            try {
                val userId = auth.currentUser?.uid ?: "anonymous"
                var photoBase64 = ""

                if (photoUri != null) {
                    photoBase64 = bitmapToBase64(photoUri, context)
                }

                // 1. KIRIM KE FIRESTORE (CLOUD)
                val orderData = hashMapOf(
                    "userId" to userId,
                    "masalah" to masalah,
                    "photoBase64" to photoBase64,
                    "status" to "Mencari Montir",
                    "timestamp" to System.currentTimeMillis()
                )

                firestore.collection("orders").add(orderData).await()
                Log.d("DEBUG_ORDER", "2. Sukses kirim ke Cloud")

                // 2. SIMPAN KE ROOM (LOKAL HISTORY) - TAMBAHAN BARU
                val localHistory = OrderHistory(
                    masalah = masalah,
                    timestamp = System.currentTimeMillis(),
                    status = "Mencari Montir"
                )
                dao.insertOrder(localHistory)
                Log.d("DEBUG_ORDER", "3. Sukses simpan ke Room Database Lokal")

                _orderState.value = OrderState.Success

            } catch (e: Exception) {
                Log.e("DEBUG_ORDER", "Error: ${e.message}")
                _orderState.value = OrderState.Error(e.message ?: "Gagal kirim order")
            }
        }
    }

    fun resetState() {
        _orderState.value = OrderState.Idle
    }
}

// ... (OrderState tetap sama) ...
sealed class OrderState {
    object Idle : OrderState()
    object Loading : OrderState()
    object Success : OrderState()
    data class Error(val message: String) : OrderState()
}