package com.teamos.launcher.assistant

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.teamos.launcher.core.Prefs
import com.teamos.launcher.databinding.ActivityOverlayBinding

/**
 * Compact assistant overlay shown above the current apps.
 *
 * Design intent: invoked by holding the power button (like Gemini/Assistant on
 * modern Androids). Intercepting the physical power long-press requires system
 * privileges / an AccessibilityService that a normal app cannot get on stock
 * Android, so in this build it is reachable from the dock and can be wired to a
 * key/accessibility hook on a rooted/AOSP build. See docs/ARCHITECTURE.md.
 */
class AssistantOverlayActivity : AppCompatActivity() {

    private lateinit var b: ActivityOverlayBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityOverlayBinding.inflate(layoutInflater)
        setContentView(b.root)

        val useGemini = Prefs(this).assistant == "gemini"
        b.overlayTitle.text = if (useGemini) "\u2728  Google Gemini" else "\uD83E\uDD16  ChatGPT"

        with(b.overlayWeb.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
        }
        b.overlayWeb.webViewClient = WebViewClient()
        b.overlayWeb.loadUrl(
            if (useGemini) "https://gemini.google.com/app" else "https://chat.openai.com/"
        )

        b.closeButton.setOnClickListener { finish() }
        b.scrim.setOnClickListener { finish() }
        // Consume taps on the sheet so they don't dismiss the overlay.
        b.sheet.setOnClickListener { }
    }

    override fun onDestroy() {
        b.overlayWeb.destroy()
        super.onDestroy()
    }
}
