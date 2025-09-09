package com.hater.githubsearch.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import com.jakewharton.disklrucache.DiskLruCache
import java.io.File
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest

object ImageLoader {

    private lateinit var memoryCache: LruCache<String, Bitmap>
    private lateinit var diskCache: DiskLruCache
    private const val DISK_CACHE_SIZE = 1024 * 1024 * 50 // 50MB

    fun init(context: Context) {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }

        val cacheDir = getDiskCacheDir(context, "images")
        diskCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE.toLong())
    }

    suspend fun loadImage(url: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (url.isEmpty()) return null
        val key = urlToKey(url)
        memoryCache.get(key)?.let { return it }

        val diskBitmap = getBitmapFromDiskCache(key)
        if (diskBitmap != null) {
            memoryCache.put(key, diskBitmap)
            return diskBitmap
        }

        return withContext(Dispatchers.IO) {
            try {
                val byteArray = URL(url).openStream().use { it.readBytes() }

                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)

                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

                options.inJustDecodeBounds = false
                val resultBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)

                addBitmapToCache(key, resultBitmap)
                resultBitmap

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private suspend fun getBitmapFromDiskCache(key: String): Bitmap? = withContext(Dispatchers.IO) {
        diskCache.get(key)?.let { snapshot ->
            val inputStream = snapshot.getInputStream(0)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap
        }
    }

    private suspend fun addBitmapToCache(key: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        if (memoryCache.get(key) == null) {
            memoryCache.put(key, bitmap)
        }

        diskCache.edit(key)?.let { editor ->
            val out = editor.newOutputStream(0)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            editor.commit()
            out.close()
        }
    }

    private fun getDiskCacheDir(context: Context, uniqueName: String): File {
        val cachePath = context.cacheDir.path
        return File(cachePath + File.separator + uniqueName)
    }

    private fun urlToKey(url: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(url.toByteArray())).toString(16).padStart(32, '0')
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val pairResult: Pair<Int, Int> = Pair(options.outHeight, options.outWidth)
        var inSampleSize = 1

        val height: Int = pairResult.first
        val width: Int = pairResult.second

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }


}