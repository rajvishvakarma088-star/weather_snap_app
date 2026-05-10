package com.weathersnap.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import javax.inject.Inject

data class CaptureResult(
    val photoPath: String,
    val originalSizeKb: Long,
    val compressedSizeKb: Long
)

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {
    private val _captureResult = MutableStateFlow<CaptureResult?>(null)
    val captureResult: StateFlow<CaptureResult?> = _captureResult.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun capture(context: Context, imageCapture: ImageCapture) {
        val timestamp = System.currentTimeMillis()
        val photoDir = File(context.filesDir, "photos").apply { mkdirs() }
        val originalFile = File(photoDir, "photo_$timestamp.jpg")
        val compressedFile = File(photoDir, "photo_${timestamp}_compressed.jpg")
        val options = ImageCapture.OutputFileOptions.Builder(originalFile).build()
        val executor = Executors.newSingleThreadExecutor()

        imageCapture.takePicture(
            options,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    runCatching {
                        val bitmap = decodeBitmapForCompression(originalFile.absolutePath)
                        val orientedBitmap = bitmap.applyExifOrientation(originalFile.absolutePath)
                        FileOutputStream(compressedFile).use { stream ->
                            orientedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
                        }
                        if (orientedBitmap !== bitmap) orientedBitmap.recycle()
                        bitmap.recycle()
                        _captureResult.value = CaptureResult(
                            photoPath = compressedFile.absolutePath,
                            originalSizeKb = originalFile.length() / 1024,
                            compressedSizeKb = compressedFile.length() / 1024
                        )
                    }.onFailure {
                        _error.value = it.message ?: "Could not compress photo"
                    }
                    executor.shutdown()
                }

                override fun onError(exception: ImageCaptureException) {
                    _error.value = exception.message ?: "Could not capture photo"
                    executor.shutdown()
                }
            }
        )
    }
}

private fun decodeBitmapForCompression(path: String): Bitmap {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)

    val options = BitmapFactory.Options().apply {
        inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, maxDimension = 1800)
    }
    return requireNotNull(BitmapFactory.decodeFile(path, options)) {
        "Could not decode captured photo"
    }
}

private fun calculateSampleSize(width: Int, height: Int, maxDimension: Int): Int {
    var sampleSize = 1
    var largest = maxOf(width, height)
    while (largest / sampleSize > maxDimension) {
        sampleSize *= 2
    }
    return sampleSize
}

private fun Bitmap.applyExifOrientation(path: String): Bitmap {
    val rotation = when (
        ExifInterface(path).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> 0f
    }
    if (rotation == 0f) return this

    val matrix = Matrix().apply { postRotate(rotation) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
