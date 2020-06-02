package com.kay.appdemo.cache

class FileNameGenerator {
    fun generate(imageUri: String): String {
        return imageUri.hashCode().toString()
    }
}