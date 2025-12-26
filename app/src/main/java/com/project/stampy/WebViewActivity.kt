package com.project.stampy

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

/**
 * 인앱 브라우저 (WebView)
 * 이용약관 등 외부 링크를 앱 내에서 표시
 */
class WebViewActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var webView: WebView

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        initViews()
        setupWebView()
        loadUrl()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        webView = findViewById(R.id.webview)

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportZoom(true)
            builtInZoomControls = false
        }

        webView.webViewClient = WebViewClient()
    }

    private fun loadUrl() {
        val url = intent.getStringExtra(EXTRA_URL) ?: return
        webView.loadUrl(url)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}