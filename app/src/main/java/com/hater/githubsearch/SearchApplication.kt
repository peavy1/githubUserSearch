package com.hater.githubsearch

import android.app.Application
import com.hater.githubsearch.util.ImageLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SearchApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        ImageLoader.init(this)
    }
}