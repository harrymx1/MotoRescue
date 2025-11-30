package com.modulpapb.motorescue.model

import com.google.android.gms.maps.model.LatLng

data class Montir(
    val id: String,
    val nama: String,
    val lokasi: LatLng,
    val keahlian: String
)

// Data Dummy di sekitar Monas
val listMontirPalsu = listOf(
    Montir("1", "Bengkel Pak Budi", LatLng(-6.1750, 106.8275), "Ban Bocor"),
    Montir("2", "Jaya Motor", LatLng(-6.1760, 106.8265), "Mesin"),
    Montir("3", "Tambal 24 Jam", LatLng(-6.1745, 106.8260), "Semua Bisa")
)