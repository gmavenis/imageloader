package com.kay.appdemo.support

import java.io.IOException
import java.io.InputStream

class CustomInputStream(private val stream: InputStream, private val length: Int) : InputStream() {
    override fun available(): Int {
        return length
    }

    @Throws(IOException::class)
    override fun close() {
        stream.close()
    }

    override fun mark(readLimit: Int) {
        stream.mark(readLimit)
    }

    @Throws(IOException::class)
    override fun read(): Int {
        return stream.read()
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray): Int {
        return stream.read(buffer)
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, byteOffset: Int, byteCount: Int): Int {
        return stream.read(buffer, byteOffset, byteCount)
    }

    @Throws(IOException::class)
    override fun reset() {
        stream.reset()
    }

    @Throws(IOException::class)
    override fun skip(byteCount: Long): Long {
        return stream.skip(byteCount)
    }

    override fun markSupported(): Boolean {
        return stream.markSupported()
    }

}