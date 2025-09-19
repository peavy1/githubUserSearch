package com.hater.githubsearch.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.net.URLEncoder

object ImageLoader {
    private lateinit var memoryCache: LruCache<String, Bitmap>
    private lateinit var diskCache: CustomDiskCache
    private const val DISK_CACHE_SIZE = 1024 * 1024 * 50

    fun init(context: Context) {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }

        val cacheDir = getDiskCacheDir(context, "images")
        diskCache = CustomDiskCache(cacheDir, DISK_CACHE_SIZE.toLong())
    }

    suspend fun loadImage(url: String): Bitmap? {
        if (url.isEmpty()) return null
        val key = urlToKey(url)

        memoryCache.get(key)?.let {
            return it
        }

        val diskBitmap = getBitmapFromDiskCache(key)
        diskBitmap?.let {
            memoryCache.put(key, diskBitmap)
            return diskBitmap
        }

        return withContext(Dispatchers.IO) {
            try {
                val networkBitmap = URL(url).openStream().use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
                networkBitmap?.let {
                    addBitmapToCaches(key, it)
                }
                networkBitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private suspend fun getBitmapFromDiskCache(key: String): Bitmap? = withContext(Dispatchers.IO) {
        diskCache.getFile(key)?.let { file ->
            BitmapFactory.decodeFile(file.absolutePath)
        }
    }

    private suspend fun addBitmapToCaches(key: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        if (memoryCache.get(key) == null) {
            memoryCache.put(key, bitmap)
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        diskCache.put(key, byteArray)
    }

    private fun getDiskCacheDir(context: Context, uniqueName: String): File {
        val cachePath = context.cacheDir.path
        return File(cachePath + File.separator + uniqueName)
    }

    private fun urlToKey(url: String): String {
        return URLEncoder.encode(url, "UTF-8")
    }
}

