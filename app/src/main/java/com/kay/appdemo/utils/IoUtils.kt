package com.kay.appdemo.utils

import com.kay.appdemo.interfaces.ByteCopyListener
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/** {@value}  */
const val DEFAULT_BUFFER_SIZE = 10 * 1024 // 32 KB

/** {@value}  */
const val DEFAULT_IMAGE_TOTAL_SIZE = 500 * 1024 // 500 Kb

/** {@value}  */
const val CONTINUE_LOADING_PERCENTAGE = 75


@Throws(IOException::class)
fun copyStream(inputStream: InputStream, os: OutputStream, listener: ByteCopyListener?): Boolean {
    return copyStream(inputStream, os, listener, DEFAULT_BUFFER_SIZE)
}

@Throws(IOException::class)
fun copyStream(inputStream: InputStream, os: OutputStream, listener: ByteCopyListener?, bufferSize: Int): Boolean {
    var current = 0
    var total: Int = inputStream.available()
    if (total <= 0) {
        total = DEFAULT_IMAGE_TOTAL_SIZE
    }
    val bytes = ByteArray(bufferSize)
    var count = 0
    if (shouldStopLoading(listener, current, total)) return false
    while (inputStream.read(bytes, 0, bufferSize).also { count = it } != -1) {
        os.write(bytes, 0, count)
        current += count
        if (shouldStopLoading(listener, current, total)) return false
    }
    os.flush()
    return true
}

private fun shouldStopLoading(listener: ByteCopyListener?, current: Int, total: Int): Boolean {
    if (listener != null) {
        val shouldContinue = listener.onBytesCopied(current, total)
        if (!shouldContinue) {
            if (100 * current / total < CONTINUE_LOADING_PERCENTAGE) {
                return true // if loaded more than 75% then continue loading anyway
            }
        }
    }
    return false
}

fun closeSafely(closeable: Closeable?) {
    if (closeable != null) {
        try {
            closeable.close()
        } catch (ignored: Exception) {
        }
    }
}