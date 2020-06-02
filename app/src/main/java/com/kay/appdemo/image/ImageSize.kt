package com.kay.appdemo.image

class ImageSize(val width: Int, val height: Int) {
    fun scaleDown(sampleSize: Int) =
        ImageSize(width / sampleSize, height / sampleSize)
}