package com.hater.githubsearch.util
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.WeakHashMap


private val imageJobMap = WeakHashMap<ImageView, Job>()
fun ImageView.loadUrl(url: String?) {
    imageJobMap[this]?.cancel()

    setImageBitmap(null)
    if (url.isNullOrEmpty()) return

    val job = CoroutineScope(Dispatchers.Main).launch {
        ImageLoader.loadImage(url)?.let { bitmap ->
            setImageBitmap(bitmap)
        }
    }
    imageJobMap[this] = job

    job.invokeOnCompletion {
        imageJobMap.remove(this)
    }
}