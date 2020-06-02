package com.kay.appdemo.cache

import com.kay.appdemo.interfaces.ByteCopyListener
import com.kay.appdemo.utils.closeSafely
import com.kay.appdemo.utils.copyStream
import java.io.*

class DiskCache(private val cacheDir: File, private val fileNameGenerator: FileNameGenerator) {


    fun get(imageUri: String): File {
        val fileName: String = fileNameGenerator.generate(imageUri)
        return File(cacheDir, fileName)
    }

    fun save(imageUri: String, imageStream: InputStream, copyListener: ByteCopyListener): Boolean {
        val imageFile: File = get(imageUri)
        val tmpFile = File(imageFile.absolutePath + ".tmp")
        var loaded = false
        try {
            val os: OutputStream = BufferedOutputStream(FileOutputStream(tmpFile), DEFAULT_BUFFER_SIZE)
            loaded = try {
                copyStream(imageStream, os, copyListener, DEFAULT_BUFFER_SIZE)
            } finally {
                closeSafely(os)
            }
        } finally {
            if (loaded && !tmpFile.renameTo(imageFile)) {
                loaded = false
            }
            if (!loaded) {
                tmpFile.delete()
            }
        }
        return loaded
    }

    fun clear() {
        val files: Array<File>? = cacheDir.listFiles()
        if (files != null) {
            for (f in files) {
                f.delete()
            }
        }
    }

}