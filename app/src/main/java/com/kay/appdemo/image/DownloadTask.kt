package com.kay.appdemo.image

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.util.LruCache
import com.kay.appdemo.interfaces.ByteCopyListener
import com.kay.appdemo.cache.DiskCache
import com.kay.appdemo.support.CustomInputStream
import com.kay.appdemo.utils.closeSafely
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Download image, report progress and decode bitmap
 */
class DownloadTask(
    private val imageJob: ImageJob,
    private val diskCache: DiskCache,
    private val memCache: LruCache<String, Bitmap>

) : Runnable, ByteCopyListener {

    private val ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
    private val connectTimeout = 5000 //5s
    private val readTimeout = 20000 //20s

    override fun run() {
        try {
            ImagesWorker.checkInterruptedTask(imageJob.imageHolder, imageJob.memCacheKey)
            if (downloadAndCacheOnDisk()) {
                ImagesWorker.checkInterruptedTask(imageJob.imageHolder, imageJob.memCacheKey)
                Log.d("DownloadTask", "File downloaded: ")
                val decodeFileTask = DecodeFileTask(
                    imageJob,
                    diskCache,
                    memCache
                )
                ImagesWorker.continueWithCacheTask(decodeFileTask)
            } else {
                Log.d("DownloadTask", "Fail to cache")
            }
        } catch (e: InterruptedException) {
            Log.e("DownloadTask", "Job interrupted")
            return
        } catch (e: IOException) {
            Log.e("DownloadTask", "Fail to download")
        }

    }

    @Throws(IOException::class)
    private fun downloadAndCacheOnDisk(): Boolean {
        val inputStream: InputStream = openNetworkStream(imageJob.imageUri)
        return try {
            diskCache.save(imageJob.imageUri, inputStream, this)
        } finally {
            closeSafely(inputStream)
        }

    }


    @Throws(IOException::class)
    private fun openNetworkStream(imageUri: String): InputStream {
        val conn = createHttpConnection(imageUri)
        val imageStream: InputStream
        imageStream = try {
            conn.inputStream
        } catch (e: IOException) {
            throw e
        }
        if (conn.responseCode != 200) {
            closeSafely(imageStream)
            throw IOException("Http error")
        }
        return CustomInputStream(BufferedInputStream(imageStream, com.kay.appdemo.utils.DEFAULT_BUFFER_SIZE), conn.contentLength)
    }

    private fun createHttpConnection(imageUri: String): HttpURLConnection {
        val encodedUrl: String = Uri.encode(imageUri, ALLOWED_URI_CHARS)
        val conn: HttpURLConnection = URL(encodedUrl).openConnection() as HttpURLConnection
        conn.connectTimeout = connectTimeout
        conn.readTimeout = readTimeout
        return conn
    }

    override fun onBytesCopied(current: Int, total: Int): Boolean {
        if (!ImagesWorker.isViewUsedForOtherTask(imageJob.imageHolder, imageJob.memCacheKey)) {
            ImagesWorker.fireTaskWithHandler(Runnable {
                imageJob.progressUpdateListener
                    .updateProgress(imageJob.imageUri, current, total);
            })

        }
        return true
    }


}