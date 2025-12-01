package com.modulpapb.motorescue.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.modulpapb.motorescue.ui.home.HomeScreen
import com.modulpapb.motorescue.ui.login.LoginScreen

@Composable
fun MotoRescueApp() {
    // 1. Membuat Controller (Supir)
    val navController = rememberNavController()

    // 2. Mengatur Rute (Peta Jalan)
    NavHost(navController = navController, startDestination = "login") {

        // Rute ke Halaman Login
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // Saat login sukses, pindah ke home dan hapus riwayat login (biar gak bisa back)
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Rute ke Halaman Home
        composable("home") {
            HomeScreen(
                onSosClick = { navController.navigate("order_form") },
                onHistoryClick = { navController.navigate("history") } // <--- Sambungkan
            )
        }

        // Tambahkan Rute Baru (Formulir Order) - Masih Kosong
        composable("order_form") {
            com.modulpapb.motorescue.ui.order.OrderScreen(
                onOrderSubmitted = {
                    // Sementara kita balik ke Home dulu atau Toast
                    // Nanti Tahap selanjutnya kita simpan ke Firestore
                    navController.popBackStack()
                }
            )
        }

        // Rute Riwayat
        composable("history") {
            com.modulpapb.motorescue.ui.history.HistoryScreen()
        }
    }
}