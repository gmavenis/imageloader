package com.kay.appdemo.interfaces

interface ProgressUpdateListener {
    fun updateProgress(imageUri: String, current: Int, total: Int)
}