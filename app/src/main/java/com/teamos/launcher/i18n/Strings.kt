package com.teamos.launcher.i18n

import android.content.Context
import com.teamos.launcher.core.Prefs
import org.json.JSONObject

data class LocaleInfo(val code: String, val name: String, val flag: String)

/**
 * System string catalog loaded from assets/i18n/<locale>.json.
 * Falls back to the default locale (pt-BR) for any missing key/locale.
 */
object Strings {

    private val cache = HashMap<String, Map<String, String>>()
    private var localesList: List<LocaleInfo>? = null
    private var defaultLocale: String = "pt-BR"

    fun locales(context: Context): List<LocaleInfo> {
        ensureIndex(context)
        return localesList ?: emptyList()
    }

    /** Resolve a key using the user's chosen locale, then the default locale. */
    fun get(context: Context, key: String): String {
        val chosen = Prefs(context).locale
        table(context, chosen)[key]?.let { return it }
        table(context, defaultLocale)[key]?.let { return it }
        return key
    }

    fun forLocale(context: Context, locale: String, key: String): String {
        table(context, locale)[key]?.let { return it }
        table(context, defaultLocale)[key]?.let { return it }
        return key
    }

    private fun ensureIndex(context: Context) {
        if (localesList != null) return
        val json = context.assets.open("i18n/index.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        defaultLocale = root.optString("defaultLocale", "pt-BR")
        val arr = root.getJSONArray("locales")
        val list = mutableListOf<LocaleInfo>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(LocaleInfo(o.getString("code"), o.getString("name"), o.optString("flag", "")))
        }
        localesList = list
    }

    private fun table(context: Context, locale: String): Map<String, String> {
        cache[locale]?.let { return it }
        val map = HashMap<String, String>()
        try {
            val json = context.assets.open("i18n/$locale.json").bufferedReader().use { it.readText() }
            val obj = JSONObject(json)
            for (k in obj.keys()) map[k] = obj.getString(k)
        } catch (_: Exception) {
            // Missing locale file — leave map empty so callers fall back.
        }
        cache[locale] = map
        return map
    }
}
