package com.modulpapb.motorescue.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.modulpapb.motorescue.data.AppDatabase
import com.modulpapb.motorescue.data.OrderHistory
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    // 1. Ambil Database
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.orderDao()

    // 2. Ambil Data sebagai Flow (Selalu update otomatis kalau ada data baru)
    // stateIn mengubah Flow database menjadi State yang bisa dibaca UI
    val historyList: StateFlow<List<OrderHistory>> = dao.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}