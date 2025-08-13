package com.hater.githubsearch

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.hater.githubsearch.databinding.ActivityUserDetailBinding

class UserDetailActivity: AppCompatActivity() {

    private val binding: ActivityUserDetailBinding by lazy {
        ActivityUserDetailBinding.inflate(layoutInflater)
    }
    private lateinit var callback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val userUrl = intent.getStringExtra(MainActivity.KEY_USER_URL) ?: ""
        binding.webview.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            loadUrl(userUrl)
        }

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(binding.webview.canGoBack()) {
                    binding.webview.goBack()
                } else {
                    callback.remove()
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }
}