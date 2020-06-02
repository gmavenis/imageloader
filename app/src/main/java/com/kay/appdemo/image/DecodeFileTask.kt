package com.kay.appdemo.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import com.kay.appdemo.cache.DiskCache
import com.kay.appdemo.support.CustomInputStream
import java.io.*


class DecodeFileTask
    (
    private val imageJob: ImageJob,
    private val diskCache: DiskCache,
    private val memCache: LruCache<String, Bitmap>
) : Runnable {

    override fun run() {
        try {
            ImagesWorker.checkInterruptedTask(imageJob.imageHolder, imageJob.memCacheKey)
            Log.d("DecodeFileTask", "Decoding file")
            val bitmap = tryLoadBitmap()
            ImagesWorker.checkInterruptedTask(imageJob.imageHolder, imageJob.memCacheKey)
            if (bitmap != null) {
                Log.d("DecodeFileTask", "storing bitmap at: ${imageJob.memCacheKey}")
                memCache.put(imageJob.memCacheKey, bitmap)
                ImagesWorker.fireTaskWithHandler(Runnable {
                    imageJob.imageHolder.setImageBitmap(bitmap)
                })
            } else {
                throw Exception("Can't decode bitmap")
            }
        }catch (e: InterruptedException){
            Log.e("DecodeFileTask", "Interrupted ")
        }catch (e: Exception){
            Log.e("DecodeFileTask", "Failed to decode ")
        }

    }

    /**
     *
     */
    private fun tryLoadBitmap(): Bitmap? {
        return try {
            val imageFile = diskCache.get(imageJob.imageUri)
            if (imageFile.exists()) {
                decodeImage(imageFile.absolutePath, imageJob.imageSize.width, imageJob.imageSize.height)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeImage(uri: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        return decodeSampledBitmapFromStream(inputStreamFromFile(uri), reqWidth, reqHeight)
    }

    private fun decodeSampledBitmapFromStream(inputStream: InputStream, reqWidth: Int, reqHeight: Int): Bitmap? {
        val outputStream = ByteArrayOutputStream()
        try {
            var n: Int
            val buffer = ByteArray(1024)
            while (inputStream.read(buffer).also { n = it } > 0) {
                outputStream.write(buffer, 0, n)
            }
            return decodeSampledBitmapFromByteArray(outputStream.toByteArray(), reqWidth, reqHeight)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun inputStreamFromFile(imageFile: String): InputStream {
        val imageStream = BufferedInputStream(FileInputStream(imageFile), DEFAULT_BUFFER_SIZE)
        return CustomInputStream(imageStream, File(imageFile).length().toInt())
    }

    /**
     * Decode a Stream
     */
    private fun decodeSampledBitmapFromByteArray(data: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(data, 0, data.size, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeByteArray(data, 0, data.size, options)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (width > reqWidth || height > reqHeight) {
            val halfWidth = width / 2
            val halfHeight = height / 2
            while (halfWidth / inSampleSize >= reqWidth && halfHeight / inSampleSize >= reqHeight) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}