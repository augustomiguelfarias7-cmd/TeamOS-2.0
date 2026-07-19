package com.teamos.launcher.web

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.teamos.launcher.core.Prefs
import com.teamos.launcher.databinding.ActivityBrowserBinding

/** Full browser with an address bar, Google as default search, and an AI sidebar. */
class BrowserActivity : AppCompatActivity() {

    private lateinit var b: ActivityBrowserBinding
    private lateinit var prefs: Prefs
    private var aiLoaded = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityBrowserBinding.inflate(layoutInflater)
        setContentView(b.root)
        prefs = Prefs(this)

        configure(b.web)
        b.web.webChromeClient = WebChromeClient()
        b.web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                if (url != null && !b.address.hasFocus()) b.address.setText(url)
            }
        }

        val start = intent.getStringExtra(WebAppActivity.EXTRA_URL) ?: homeUrl()
        b.web.loadUrl(start)

        b.address.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                b.web.loadUrl(resolve(b.address.text.toString().trim()))
                b.address.clearFocus()
                true
            } else false
        }

        b.aiButton.setOnClickListener { toggleAi() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    b.aiSidebar.visibility == android.view.View.VISIBLE -> toggleAi()
                    b.web.canGoBack() -> b.web.goBack()
                    else -> finish()
                }
            }
        })
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun toggleAi() {
        val showing = b.aiSidebar.visibility == android.view.View.VISIBLE
        b.aiSidebar.visibility = if (showing) android.view.View.GONE else android.view.View.VISIBLE
        if (!showing && !aiLoaded) {
            configure(b.aiWeb)
            b.aiWeb.webViewClient = WebViewClient()
            val ai = if (prefs.assistant == "gemini")
                "https://gemini.google.com/app" else "https://chat.openai.com/"
            b.aiWeb.loadUrl(ai)
            aiLoaded = true
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configure(w: WebView) {
        with(w.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            mediaPlaybackRequiresUserGesture = false
        }
    }

    private fun homeUrl(): String = when (prefs.searchEngine) {
        "bing" -> "https://www.bing.com/"
        "duckduckgo" -> "https://duckduckgo.com/"
        else -> "https://www.google.com/"
    }

    private fun resolve(input: String): String {
        if (input.isEmpty()) return homeUrl()
        val looksLikeUrl = input.startsWith("http://") || input.startsWith("https://") ||
            (input.contains(".") && !input.contains(" "))
        if (looksLikeUrl) {
            return if (input.startsWith("http")) input else "https://$input"
        }
        val q = Uri.encode(input)
        return when (prefs.searchEngine) {
            "bing" -> "https://www.bing.com/search?q=$q"
            "duckduckgo" -> "https://duckduckgo.com/?q=$q"
            else -> "https://www.google.com/search?q=$q"
        }
    }

    override fun onDestroy() {
        b.web.destroy()
        b.aiWeb.destroy()
        super.onDestroy()
    }
}
