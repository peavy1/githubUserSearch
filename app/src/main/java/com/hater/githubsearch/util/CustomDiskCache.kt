package com.hater.githubsearch.util

import java.io.File
import java.io.IOException
import kotlin.collections.LinkedHashMap

class CustomDiskCache(
    private val cacheDir: File,
    private val maxSize: Long
) {
    private val lruEntries = LinkedHashMap<String, Long>(100, 0.75f,  true)
    private var currentSize: Long = 0

    init {
        cacheDir.listFiles()?.forEach { file ->
            val fileSize = file.length()
            lruEntries[file.name] = fileSize
            currentSize += fileSize
        }
        trimToSize()
    }

    @Synchronized
    fun getFile(key: String): File? {
        lruEntries[key] ?: return null
        val file = File(cacheDir, key)
        return if (file.exists()) {
            file
        } else {
            lruEntries.remove(key)
            null
        }
    }

    @Synchronized
    fun put(key: String, data: ByteArray) {
        val file = File(cacheDir, key)

        try {
            file.writeBytes(data)

            val newSize = data.size.toLong()
            val oldSize = lruEntries.remove(key) ?: 0L

            currentSize = currentSize - oldSize + newSize
            lruEntries[key] = newSize

            trimToSize()

        } catch (e: IOException) {
            e.printStackTrace()
            file.delete()
        }
    }

    @Synchronized
    private fun trimToSize() {
        val iterator = lruEntries.entries.iterator()
        while (currentSize > maxSize && iterator.hasNext()) {
            val (key, size) = iterator.next()
            if (File(cacheDir, key).delete()) {
                currentSize -= size
                iterator.remove()
            }
        }
    }

    companion object {
        operator fun invoke(cacheDir: File, maxSize: Long): CustomDiskCache {
            val checkedCacheDir = cacheDir.also { it.mkdirs() }
            return CustomDiskCache(checkedCacheDir, maxSize)
        }
    }
}
