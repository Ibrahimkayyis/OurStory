package com.dicoding.picodiploma.loginwithanimation.view.addstory

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityAddStoryBinding
import com.dicoding.picodiploma.loginwithanimation.utils.uriToFile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private val addStoryViewModel: AddStoryViewModel by viewModels()

    private var photoFile: File? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isIncludeLocation = false
    private var currentLat: Double? = null
    private var currentLon: Double? = null

    companion object {
        private const val GALLERY_REQUEST_CODE = 1000
        private const val CAMERA_REQUEST_CODE = 1001
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002
        private const val MAX_IMAGE_SIZE = 1024 // Max image width/height (in px)
        private const val REQUEST_LOCATION_PERMISSION = 2000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.switchIncludeLocation.setOnCheckedChangeListener { _, isChecked ->
            isIncludeLocation = isChecked
            if (isIncludeLocation) {
                getMyLastLocation()
            } else {
                currentLat = null
                currentLon = null
            }
        }

        addStoryViewModel.uploadResult.observe(this, Observer { result ->
            result.onSuccess { message ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK) // Menandai bahwa upload berhasil
                finish()
            }
            result.onFailure { exception ->
                Toast.makeText(this, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })

        binding.btnChooseGallery.setOnClickListener {
            openGallery()
        }
        binding.btnOpenCamera.setOnClickListener {
            if (isCameraPermissionGranted()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        binding.btnUpload.setOnClickListener {
            uploadStory()
        }
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun uploadStory() {
        val description = binding.etDescription.text.toString()
        if (description.isEmpty() || photoFile == null) {
            Toast.makeText(this, "Please provide both description and photo", Toast.LENGTH_SHORT)
                .show()
            return
        }

        showLoading(true)

        val lat = if (isIncludeLocation && currentLat != null) currentLat else null
        val lon = if (isIncludeLocation && currentLon != null) currentLon else null

        addStoryViewModel.uploadStory(photoFile!!, description, lat, lon)
    }

    private fun getMyLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLat = location.latitude
                    currentLon = location.longitude
                    Toast.makeText(this, "Lokasi didapat: $currentLat, $currentLon", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getMyLastLocation()
            } else {
                Toast.makeText(this, "Izin lokasi ditolak", Toast.LENGTH_SHORT).show()
                binding.switchIncludeLocation.isChecked = false
            }
        } else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri: Uri? = data?.data
                    if (selectedImageUri != null) {
                        try {
                            // Resize gambar dari URI
                            val resizedBitmap = resizeImage(selectedImageUri)
                            photoFile = bitmapToFile(resizedBitmap)
                            binding.ivPreview.setImageBitmap(resizedBitmap)
                        } catch (e: Exception) {
                            Toast.makeText(this, "Failed to process image: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                    }
                }

                CAMERA_REQUEST_CODE -> {
                    val capturedImage: Bitmap? = data?.extras?.get("data") as Bitmap?
                    if (capturedImage != null) {
                        val resizedBitmap = resizeImage(capturedImage)
                        photoFile = bitmapToFile(resizedBitmap)
                        binding.ivPreview.setImageBitmap(resizedBitmap)
                    }
                }
            }
        }
    }

    private fun resizeImage(uri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        return resizeImage(originalBitmap)
    }

    private fun resizeImage(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratio = width.toFloat() / height.toFloat()
        var newWidth = MAX_IMAGE_SIZE
        var newHeight = MAX_IMAGE_SIZE

        if (width > height) {
            newHeight = (newWidth / ratio).toInt()
        } else {
            newWidth = (newHeight * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun bitmapToFile(bitmap: Bitmap): File {
        val file = File(cacheDir, "temp_image.jpg")
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
