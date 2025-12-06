package com.papb.motorescue

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.papb.motorescue.data.model.RescueRequest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dummyData = listOf(
            RescueRequest(id = "1", driverName = "Harry", status = "WAITING", problemDesc = "Ban Bocor"),
            RescueRequest(id = "2", driverName = "Budi", status = "ACCEPTED", problemDesc = "Mogok"),
            RescueRequest(id = "3", driverName = "Siti", status = "WAITING", problemDesc = "Rantai Putus")
        )

        val orderanMasuk = dummyData.filter { laporan ->
            laporan.status == "WAITING"
        }

        Log.d("TES_MOTO", "=== DAFTAR ORDERAN MASUK ===")
        orderanMasuk.forEach { data ->
            Log.d("TES_MOTO", "Pengemudi: ${data.driverName} | Masalah: ${data.problemDesc}")
        }

        setContent {
            Text(text = "Cek Logcat dengan kata kunci: TES_MOTO")
        }
    }
}