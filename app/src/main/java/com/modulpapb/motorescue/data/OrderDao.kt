package com.modulpapb.motorescue.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    // Simpan data baru
    @Insert
    suspend fun insertOrder(order: OrderHistory)

    // Ambil semua data (diurutkan dari yang terbaru)
    @Query("SELECT * FROM history_table ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<OrderHistory>>
}