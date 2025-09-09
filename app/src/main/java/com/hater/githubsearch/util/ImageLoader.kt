package com.hater.githubsearch.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

object ImageLoader {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }
    suspend fun loadImage(url: String): Bitmap? {
        if (url.isEmpty()) return null

        val cachedBitmap = memoryCache.get(url)
        if (cachedBitmap != null) {
            return cachedBitmap
        }

        return withContext(Dispatchers.IO) {
            try {
                val decodedBitmap = BitmapFactory.decodeStream(URL(url).openStream())
                memoryCache.put(url, decodedBitmap)
                decodedBitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
