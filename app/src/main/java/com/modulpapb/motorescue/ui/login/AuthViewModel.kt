package com.modulpapb.motorescue.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // State untuk UI (Loading, Error, Sukses)
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Cek apakah user sudah login sebelumnya (Auto Login)
    fun checkLoginStatus() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.Success(auth.currentUser!!)
        }
    }

    // Fungsi Login
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, pass).await()
                _authState.value = AuthState.Success(result.user!!)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login Gagal")
            }
        }
    }

    // Fungsi Register (Daftar Baru)
    fun register(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, pass).await()
                _authState.value = AuthState.Success(result.user!!)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Register Gagal")
            }
        }
    }

    // Fungsi Logout
    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
}

// Status-status yang mungkin terjadi
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}