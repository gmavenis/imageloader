package com.kay.appdemo.utils

import android.content.Context
import com.kay.appdemo.cache.DiskCache
import com.kay.appdemo.image.ImageSize
import java.io.File

/**
 * find a file in cache
 */
fun String.getMemCacheKey(size: ImageSize):String{
    return "${this}_${size.width}x${size.height}"
}

fun String.findInCache(diskCache: DiskCache): File? {
    val image: File? = diskCache.get(this)
    return if (image != null && image.exists()) image else null
}

/**
 * cache dir
 */
fun getCacheDir(context: Context): File{
    val cacheDirPath = "/data/data/" + context.packageName.toString() + "/cache/"
    return File(cacheDirPath)
}
