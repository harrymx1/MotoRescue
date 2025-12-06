package com.papb.motorescue.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rescue_request")
data class RescueRequest(
    @PrimaryKey
    val id: String = "",

    val driverName: String = "",
    val problemDesc: String = "",
    val photoUrl: String = "",

    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",

    val status: String = "WAITING",

    val mechanicName: String? = null
)