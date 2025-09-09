package com.hater.githubsearch.util

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AppUtil {

    fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    fun <T> debounce(
        timeMillis: Long = 2000L,
        coroutineScope: CoroutineScope,
        block: (T) -> Unit
    ): (T) -> Unit {
        var debounceJob: Job? = null
        return { param ->
            debounceJob?.cancel()
            debounceJob = coroutineScope.launch {
                delay(timeMillis)
                block(param)
            }
        }
    }

}