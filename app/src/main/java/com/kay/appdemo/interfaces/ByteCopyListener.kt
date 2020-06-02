package com.kay.appdemo.interfaces

interface ByteCopyListener {
    fun onBytesCopied(current: Int, total: Int): Boolean
}