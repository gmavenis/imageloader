package com.kay.appdemo.image

import com.kay.appdemo.interfaces.ProgressUpdateListener

class ImageJob(
    val imageUri: String,
    val imageHolder: ImageHolder,
    val imageSize: ImageSize,
    val memCacheKey: String,
    val progressUpdateListener: ProgressUpdateListener
) {


}