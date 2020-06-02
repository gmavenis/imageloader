package com.kay.appdemo.image

import android.graphics.Bitmap
import android.view.View
import java.lang.ref.WeakReference

interface ImageHolder {
    /**
     * Set bitmap
     */
    fun setImageBitmap(bitmap: Bitmap)

    /**
     * width, used when decoding
     */
    fun width(): Int

    /**
     * height, used when decoding
     */
    fun height(): Int

    /**
     * Use as key for cache
     */
    fun getIdentifier(): Int
}

/**
 * Avoid View's leak
 */
class WeakImageHolder(strongRef: ImageHolder) : ImageHolder {
    private val weakRef = WeakReference<ImageHolder>(strongRef)

    override fun setImageBitmap(bitmap: Bitmap) {
        weakRef.get()?.setImageBitmap(bitmap)
    }

    override fun width(): Int {
        return weakRef.get()?.width() ?: 300
    }

    override fun height(): Int {
        return weakRef.get()?.height() ?: 300
    }

    override fun getIdentifier(): Int {
        return weakRef.get()?.getIdentifier() ?: Int.MIN_VALUE
    }
}