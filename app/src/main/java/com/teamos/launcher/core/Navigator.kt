package com.teamos.launcher.core

import android.content.Context
import android.content.Intent
import com.teamos.launcher.apps.GalleryActivity
import com.teamos.launcher.apps.NotesActivity
import com.teamos.launcher.apps.SimActivity
import com.teamos.launcher.apps.SosActivity
import com.teamos.launcher.apps.WellbeingActivity
import com.teamos.launcher.data.AppEntry
import com.teamos.launcher.settings.SettingsActivity
import com.teamos.launcher.store.AppStoreActivity
import com.teamos.launcher.teamai.TeamAiActivity
import com.teamos.launcher.web.BrowserActivity
import com.teamos.launcher.web.WebAppActivity

/** Central place that turns an AppEntry (or teamos:// route) into the right screen. */
object Navigator {

    fun open(context: Context, app: AppEntry) {
        if (app.isSystemScheme) openRoute(context, app.url) else openWeb(context, app.url, app.name)
    }

    fun openRoute(context: Context, route: String) {
        val intent = when (route.removePrefix("teamos://")) {
            "store" -> Intent(context, AppStoreActivity::class.java)
            "settings" -> Intent(context, SettingsActivity::class.java)
            "browser" -> Intent(context, BrowserActivity::class.java)
            "team-ai" -> Intent(context, TeamAiActivity::class.java)
            "notes" -> Intent(context, NotesActivity::class.java)
            "gallery" -> Intent(context, GalleryActivity::class.java)
            "wellbeing" -> Intent(context, WellbeingActivity::class.java)
            "sim" -> Intent(context, SimActivity::class.java)
            "sos" -> Intent(context, SosActivity::class.java)
            else -> Intent(context, AppStoreActivity::class.java)
        }
        context.startActivity(intent)
    }

    fun openWeb(context: Context, url: String, title: String = "") {
        val intent = Intent(context, WebAppActivity::class.java)
            .putExtra(WebAppActivity.EXTRA_URL, url)
            .putExtra(WebAppActivity.EXTRA_TITLE, title)
        context.startActivity(intent)
    }
}
