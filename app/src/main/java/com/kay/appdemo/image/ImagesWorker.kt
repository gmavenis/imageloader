package com.kay.appdemo.image

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.LruCache
import com.kay.appdemo.App
import com.kay.appdemo.interfaces.ProgressUpdateListener
import com.kay.appdemo.cache.DiskCache
import com.kay.appdemo.cache.FileNameGenerator
import com.kay.appdemo.utils.getCacheDir
import com.kay.appdemo.utils.getMemCacheKey
import java.util.concurrent.*

object ImagesWorker {
    // avail cpu cores
    private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors()

    private val cacheViewAndUri = hashMapOf<Int, String>()

    private var downloadedByteCount = 0

    // lru mem cache size
    private val memCache = LruCache<String, Bitmap>(10)

    // Sets the initial threadpool size to 8
    private const val CORE_POOL_SIZE = 8

    // Sets the maximum threadpool size to 8
    private const val MAXIMUM_POOL_SIZE = 8

    // disk cache
    private val diskCache = DiskCache(
        getCacheDir(App.instance.applicationContext)
        , FileNameGenerator()
    )

    private val handler = Handler(Looper.getMainLooper())

    // A queue of Runnables for the image download pool
    private val workerQueue: BlockingQueue<Runnable> = LinkedBlockingQueue()

    // A queue of Runnables for the image processing pool
    private val cacheQueue: BlockingQueue<Runnable> = LinkedBlockingQueue()

    private val taskDistributor: ThreadPoolExecutor = ThreadPoolExecutor(
        0, Int.MAX_VALUE, 1, TimeUnit.SECONDS,
        SynchronousQueue<Runnable>()
    )

    // A managed pool of background downloading
    private val downloadTaskExecutor: ThreadPoolExecutor = ThreadPoolExecutor(
        CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
        1, TimeUnit.SECONDS, workerQueue
    )

    // A managed pool of background cache processing
    private val cachedTaskExecutor: ThreadPoolExecutor = ThreadPoolExecutor(
        CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
        1, TimeUnit.SECONDS, cacheQueue
    )

    private val progressUpdateListener = object : ProgressUpdateListener {
        override fun updateProgress(imageUri: String, current: Int, total: Int) {
            synchronized(this) {
                downloadedByteCount += current
            }
        }
    }


    // An object that manages Messages in a Thread
    private val mHandler: Handler? = null

    fun display(
        imageHolder: ImageHolder,
        imageUri: String,
        progressUpdateListener: ProgressUpdateListener,
        imageSize: ImageSize = ImageSize(imageHolder.width(), imageHolder.height())
    ) {
        Log.d("ImagesWorker", "Start tasks: $imageUri")
        val memCacheKey = imageUri.getMemCacheKey(imageSize)

        // will retrieve later
        putTaskKey(imageHolder, memCacheKey)

        val bitMap = memCache.get(memCacheKey)
        if (bitMap != null && !bitMap.isRecycled) {//cache
            // do it onMain thread
            Log.d("ImagesWorker", "memory cache found!")
            imageHolder.setImageBitmap(bitMap)
        } else {
            val imageJob = ImageJob(
                imageUri,
                imageHolder,
                imageSize,
                memCacheKey,
                progressUpdateListener
            )
            checkFileCacheOrDownload(imageJob)
        }
    }

    private fun checkFileCacheOrDownload(imageJob: ImageJob) {

        taskDistributor.execute {
            val file = diskCache.get(imageJob.imageUri)
            val isFileCached = file.exists()
            if (isFileCached) {
                Log.d("ImagesWorker", "Cached found!")
                val decodeFileTask = DecodeFileTask(
                    imageJob,
                    diskCache,
                    memCache
                )
                cachedTaskExecutor.execute(decodeFileTask)
            } else {
                Log.d("ImagesWorker", "Cached not found!")
                Log.d("ImagesWorker", "Start downloading")
                val task = DownloadTask(
                    imageJob,
                    diskCache,
                    memCache
                )
                downloadTaskExecutor.execute(task)
            }
        }

    }

    fun continueWithCacheTask(task: Runnable) {
        cachedTaskExecutor.execute(task)
    }


    fun fireTaskWithHandler(task: Runnable) {
        handler.post(task)
    }

    private fun putTaskKey(holder: ImageHolder, key: String) {
        cacheViewAndUri[holder.getIdentifier()] = key
    }

    private fun getTaskKey(holder: ImageHolder): String? = cacheViewAndUri[holder.getIdentifier()]


    fun isViewUsedForOtherTask(imageHolder: ImageHolder, memCacheKey: String): Boolean {
        val inProgressKey = getTaskKey(imageHolder)
        if (memCacheKey != inProgressKey) {
            Log.d("ImagesWorker", "diff: $inProgressKey - $memCacheKey")
            return true
        }
        return false
    }

    @Throws(InterruptedException::class)
    fun checkInterruptedTask(imageHolder: ImageHolder, memCacheKey: String) {
        if (isViewUsedForOtherTask(imageHolder, memCacheKey))
            throw InterruptedException()
    }

    fun clearDiskCache() {
        memCache.evictAll()
        taskDistributor.execute { diskCache.clear() }
    }

}