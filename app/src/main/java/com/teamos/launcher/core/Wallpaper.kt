package com.teamos.launcher.core

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import com.teamos.launcher.R
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Device wallpaper. "default" uses the bundled gradient; "photo" downloads a free
 * wallpaper from picsum.photos (no API key) and caches it on disk.
 */
object Wallpaper {

    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    private fun file(context: Context): File = File(context.filesDir, "wallpaper.jpg")

    /** Apply the current wallpaper to a launcher background ImageView. */
    fun apply(context: Context, view: ImageView) {
        val prefs = Prefs(context)
        if (prefs.wallpaperMode != "photo") {
            view.setImageResource(R.drawable.bg_gradient)
            return
        }
        val f = file(context)
        if (f.exists() && f.length() > 0) {
            view.setImageBitmap(BitmapFactory.decodeFile(f.absolutePath))
        } else {
            view.setImageResource(R.drawable.bg_gradient)
            downloadCurrent(context) { apply(context, view) }
        }
    }

    /** Pick a new random photo wallpaper and download it. */
    fun shuffle(context: Context, onReady: () -> Unit) {
        val prefs = Prefs(context)
        prefs.wallpaperMode = "photo"
        prefs.wallpaperSeed = (1..100000).random()
        downloadCurrent(context, onReady)
    }

    fun useDefault(context: Context) {
        Prefs(context).wallpaperMode = "default"
    }

    private fun downloadCurrent(context: Context, onReady: () -> Unit) {
        val seed = Prefs(context).wallpaperSeed.let { if (it == 0) 1 else it }
        val appCtx = context.applicationContext
        io.execute {
            val ok = runCatching {
                val url = "https://picsum.photos/seed/teamos$seed/1080/2400"
                val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 12000
                    readTimeout = 12000
                    instanceFollowRedirects = true
                }
                conn.inputStream.use { input ->
                    file(appCtx).outputStream().use { out -> input.copyTo(out) }
                }
                file(appCtx).length() > 0
            }.getOrDefault(false)
            if (ok) main.post { onReady() }
        }
    }
}
