package com.teamos.launcher.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import com.teamos.launcher.R
import com.teamos.launcher.data.AppEntry
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Loads real app logos: bundled vector drawables for system apps, and the site's
 * real favicon (fetched + cached) for web apps. Falls back to a MonogramDrawable
 * (brand-colored initial) — never an emoji.
 */
object IconLoader {

    private val mem = object : LruCache<String, Bitmap>(4 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }
    private val io = Executors.newFixedThreadPool(4)
    private val main = Handler(Looper.getMainLooper())

    fun into(context: Context, app: AppEntry, view: ImageView) {
        view.tag = app.id

        val sysRes = systemIconRes(app.id)
        if (sysRes != 0) {
            view.setImageResource(sysRes)
            return
        }

        mem.get(app.id)?.let {
            view.setImageBitmap(it)
            return
        }

        view.setImageDrawable(MonogramDrawable(app.name, app.id))

        val host = Uri.parse(app.url).host ?: return
        val appCtx = context.applicationContext
        io.execute {
            val bmp = fromDisk(appCtx, app.id) ?: fromNetwork(appCtx, host, app.id)
            if (bmp != null) {
                mem.put(app.id, bmp)
                main.post { if (view.tag == app.id) view.setImageBitmap(bmp) }
            }
        }
    }

    private fun dir(context: Context): File =
        File(context.cacheDir, "icons").apply { mkdirs() }

    private fun fromDisk(context: Context, id: String): Bitmap? {
        val f = File(dir(context), "$id.png")
        if (!f.exists() || f.length() == 0L) return null
        return BitmapFactory.decodeFile(f.absolutePath)
    }

    private fun fromNetwork(context: Context, host: String, id: String): Bitmap? {
        val urls = listOf(
            "https://www.google.com/s2/favicons?sz=128&domain=$host",
            "https://icons.duckduckgo.com/ip3/$host.ico"
        )
        for (u in urls) {
            val bmp = download(u) ?: continue
            if (bmp.width < 8) continue
            runCatching {
                File(dir(context), "$id.png").outputStream().use {
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }
            return bmp
        }
        return null
    }

    private fun download(url: String): Bitmap? = runCatching {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8000
            readTimeout = 8000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "TeamOS")
        }
        conn.inputStream.use { BitmapFactory.decodeStream(it) }
    }.getOrNull()

    private fun systemIconRes(id: String): Int = when (id) {
        "settings" -> R.drawable.ic_app_settings
        "gallery" -> R.drawable.ic_app_gallery
        "appstore", "store" -> R.drawable.ic_app_store
        "browser" -> R.drawable.ic_app_browser
        "notes" -> R.drawable.ic_app_notes
        "digital-wellbeing", "wellbeing" -> R.drawable.ic_app_wellbeing
        "team-ai", "teamai" -> R.drawable.ic_app_teamai
        "sos" -> R.drawable.ic_app_sos
        "phone", "sim" -> R.drawable.ic_app_phone
        else -> 0
    }
}
