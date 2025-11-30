package com.modulpapb.motorescue.utils

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

fun createFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val storageDir = context.getExternalFilesDir(null) // Simpan di folder privat aplikasi
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}