package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

object ReceiptImageStorage {
    private const val RECEIPTS_DIR = "receipts"
    private const val CAMERA_TEMP_DIR = "camera_temp"
    private const val MAX_DIMENSION_PX = 1440
    private const val JPEG_QUALITY = 80

    private fun receiptsDir(context: Context): File =
        File(context.filesDir, RECEIPTS_DIR).apply { if (!exists()) mkdirs() }

    private fun cameraTempDir(context: Context): File =
        File(context.cacheDir, CAMERA_TEMP_DIR).apply { if (!exists()) mkdirs() }

    /**
     * Creates a temporary File + FileProvider Uri for ActivityResultContracts.TakePicture().
     */
    fun createTempCameraUri(context: Context): Pair<File, Uri> {
        val file = File(cameraTempDir(context), "cam_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return file to uri
    }

    /**
     * Imports a photo from a gallery Photo Picker URI or camera capture file into durable
     * app-private storage, normalizing EXIF rotation and compressing cleanly.
     * Returns the relative file name (e.g. "receipt_1700000_ab12cd.jpg").
     */
    fun saveReceiptFromUri(context: Context, sourceUri: Uri): String? {
        return try {
            val rotationDegrees = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                readExifRotation(stream)
            } ?: 0

            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return null

            val sampleSize = calculateInSampleSize(
                boundsOptions.outWidth,
                boundsOptions.outHeight,
                MAX_DIMENSION_PX
            )
            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = context.contentResolver.openInputStream(sourceUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            val oriented = rotateAndScaleBitmap(decoded, rotationDegrees, MAX_DIMENSION_PX)
            if (oriented !== decoded) {
                decoded.recycle()
            }

            val fileName = "receipt_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val destFile = File(receiptsDir(context), fileName)
            FileOutputStream(destFile).use { out ->
                oriented.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            oriented.recycle()
            fileName
        } catch (_: Exception) {
            null
        }
    }

    fun resolveReceiptFile(context: Context, receiptFileName: String?): File? {
        if (receiptFileName.isNullOrBlank()) return null
        val safeName = File(receiptFileName).name
        val file = File(receiptsDir(context), safeName)
        return if (file.exists()) file else null
    }

    fun deleteReceiptFile(context: Context, receiptFileName: String?) {
        if (receiptFileName.isNullOrBlank()) return
        val safeName = File(receiptFileName).name
        runCatching {
            File(receiptsDir(context), safeName).delete()
        }
    }

    fun encodeReceiptToBase64(context: Context, receiptFileName: String?): String? {
        val file = resolveReceiptFile(context, receiptFileName) ?: return null
        return runCatching {
            // Skip unexpectedly huge files (> 2MB) to keep JSON backups fast
            if (file.length() > 2 * 1024 * 1024) return null
            Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
        }.getOrNull()
    }

    fun writeReceiptFromBase64(context: Context, fileName: String, base64: String): String? {
        return runCatching {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val safeName = File(fileName).name.ifBlank {
                "receipt_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            }
            val destFile = File(receiptsDir(context), safeName)
            FileOutputStream(destFile).use { it.write(bytes) }
            safeName
        }.getOrNull()
    }

    private fun readExifRotation(inputStream: java.io.InputStream): Int {
        return runCatching {
            val exif = ExifInterface(inputStream)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }.getOrDefault(0)
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var inSampleSize = 1
        val largest = max(width, height)
        while (largest / (inSampleSize * 2) >= maxDim) {
            inSampleSize *= 2
        }
        return inSampleSize
    }

    private fun rotateAndScaleBitmap(source: Bitmap, rotationDegrees: Int, maxDim: Int): Bitmap {
        val matrix = Matrix()
        val largest = max(source.width, source.height)
        if (largest > maxDim) {
            val scale = maxDim.toFloat() / largest.toFloat()
            matrix.postScale(scale, scale)
        }
        if (rotationDegrees != 0) {
            matrix.postRotate(rotationDegrees.toFloat())
        }
        if (matrix.isIdentity) return source
        return Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
        )
    }
}
