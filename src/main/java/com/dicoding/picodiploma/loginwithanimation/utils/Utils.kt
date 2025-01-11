package com.dicoding.picodiploma.loginwithanimation.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

fun reduceFileImage(file: File): File {
    val bitmap = BitmapFactory.decodeFile(file.path)
    val compressedFile = File(file.parent, "compressed_" + file.name)

    val outputStream = FileOutputStream(compressedFile)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
    outputStream.flush()
    outputStream.close()

    return compressedFile
}

fun uriToFile(imageUri: Uri, context: Context): File {
    val myFile = File(context.cacheDir, "temp_image")
    val inputStream = context.contentResolver.openInputStream(imageUri)!!
    val outputStream = FileOutputStream(myFile)
    val buffer = ByteArray(1024)
    var length: Int
    while (inputStream.read(buffer).also { length = it } > 0) {
        outputStream.write(buffer, 0, length)
    }
    outputStream.close()
    inputStream.close()
    return myFile
}
