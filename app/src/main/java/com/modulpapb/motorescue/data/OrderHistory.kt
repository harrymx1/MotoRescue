package com.modulpapb.motorescue.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_table")
data class OrderHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val masalah: String,
    val timestamp: Long,
    val status: String = "Selesai"
)