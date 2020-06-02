package com.kay.appdemo.image

import android.graphics.Bitmap

sealed class ImageJobState {
    object DownloadStarted : ImageJobState()
    data class Downloading(val kb: Int) : ImageJobState()
    object DownloadComplete : ImageJobState()
    object Decoding : ImageJobState()
    data class JobDone(val bitmap: Bitmap) : ImageJobState()

}