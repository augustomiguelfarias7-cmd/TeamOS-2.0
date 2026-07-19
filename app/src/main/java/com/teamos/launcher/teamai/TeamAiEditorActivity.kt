package com.teamos.launcher.teamai

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader

/**
 * Team AI image editor/generator. Real cloud AI via Puter.js (free, no API key) loaded
 * inside a WebView from a bundled HTML page. Supports text-to-image generation plus
 * touch drawing / adding objects on a canvas, and saving the result to the gallery.
 *
 * The page is served through [WebViewAssetLoader] over an https origin
 * (https://appassets.androidplatform.net/) because Puter.js refuses to run under the
 * file:// scheme.
 */
class TeamAiEditorActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
        }

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)
        }
        webView.addJavascriptInterface(Bridge(), "TeamOS")
        webView.loadUrl("https://appassets.androidplatform.net/assets/teamai/editor.html")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    private inner class Bridge {
        @JavascriptInterface
        fun saveImage(dataUrl: String) {
            val comma = dataUrl.indexOf(',')
            if (comma < 0) return
            val bytes = Base64.decode(dataUrl.substring(comma + 1), Base64.DEFAULT)
            runCatching { saveToGallery(bytes) }
                .onFailure { runOnUiThread { Toast.makeText(this@TeamAiEditorActivity, "Falha ao salvar", Toast.LENGTH_SHORT).show() } }
        }
    }

    private fun saveToGallery(bytes: ByteArray) {
        val name = "teamai_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TeamAI")
            }
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return
        contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
