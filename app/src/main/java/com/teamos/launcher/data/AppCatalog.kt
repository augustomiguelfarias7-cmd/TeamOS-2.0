package com.teamos.launcher.data

import android.content.Context
import org.json.JSONObject

/** A single catalog entry — a lightweight WebView "app". */
data class AppEntry(
    val id: String,
    val name: String,
    val publisher: String,
    val category: String,
    val url: String,
    val icon: String,
    val description: String,
    val preinstalled: Boolean,
    val featured: Boolean,
    val system: Boolean
) {
    /** true when the app is a built-in system experience (teamos://...) rather than a website. */
    val isSystemScheme: Boolean get() = url.startsWith("teamos://")
}

data class AppCategory(val id: String, val name: String)

/** Loads and caches the app catalog from assets/apps.json. */
object AppCatalog {

    private var apps: List<AppEntry>? = null
    private var categories: List<AppCategory>? = null

    fun apps(context: Context): List<AppEntry> {
        load(context)
        return apps ?: emptyList()
    }

    fun categories(context: Context): List<AppCategory> {
        load(context)
        return categories ?: emptyList()
    }

    fun byId(context: Context, id: String): AppEntry? = apps(context).firstOrNull { it.id == id }

    /** ids that must be present on first boot (system + a few default web apps). */
    fun defaultInstalledIds(context: Context): List<String> =
        apps(context).filter { it.preinstalled || it.system }.map { it.id }

    private fun load(context: Context) {
        if (apps != null) return
        val json = context.assets.open("apps.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)

        val cats = mutableListOf<AppCategory>()
        val catArr = root.optJSONArray("categories")
        if (catArr != null) {
            for (i in 0 until catArr.length()) {
                val c = catArr.getJSONObject(i)
                cats.add(AppCategory(c.getString("id"), c.getString("name")))
            }
        }
        categories = cats

        val list = mutableListOf<AppEntry>()
        val arr = root.getJSONArray("apps")
        for (i in 0 until arr.length()) {
            val a = arr.getJSONObject(i)
            list.add(
                AppEntry(
                    id = a.getString("id"),
                    name = a.getString("name"),
                    publisher = a.optString("publisher", ""),
                    category = a.optString("category", ""),
                    url = a.getString("url"),
                    icon = a.optString("icon", "\uD83D\uDCF1"),
                    description = a.optString("description", ""),
                    preinstalled = a.optBoolean("preinstalled", false),
                    featured = a.optBoolean("featured", false),
                    system = a.optBoolean("system", false)
                )
            )
        }
        apps = list
    }
}
