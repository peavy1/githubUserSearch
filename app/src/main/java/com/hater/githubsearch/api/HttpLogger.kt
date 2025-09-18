package com.hater.githubsearch.api

import android.util.Log

object HttpLogger {
    private const val TAG = "HttpLogger"

    fun logRequest(method: String, url: String) {
        Log.d(TAG, "--> $method $url")
    }

    fun logSuccess(code: Int, url: String) {
        Log.d(TAG, "<-- $code OK $url")
    }

    fun logError(code: Int, url: String) {
        Log.e(TAG, "<-- $code Error $url")
    }

    fun logFailure(t: Throwable) {
        Log.e(TAG, "<-- HTTP FAILED: ${t.message}", t)
    }
}