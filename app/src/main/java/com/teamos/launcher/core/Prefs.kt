package com.teamos.launcher.core

import android.content.Context

/**
 * Lightweight persistent settings and installed-app state for TeamOS.
 * Backed by SharedPreferences — no login/account is used anywhere in the system.
 */
class Prefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("teamos", Context.MODE_PRIVATE)

    var setupComplete: Boolean
        get() = sp.getBoolean(KEY_SETUP, false)
        set(value) = sp.edit().putBoolean(KEY_SETUP, value).apply()

    var locale: String
        get() = sp.getString(KEY_LOCALE, DEFAULT_LOCALE) ?: DEFAULT_LOCALE
        set(value) = sp.edit().putString(KEY_LOCALE, value).apply()

    /** "chatgpt" or "gemini" */
    var assistant: String
        get() = sp.getString(KEY_ASSISTANT, "chatgpt") ?: "chatgpt"
        set(value) = sp.edit().putString(KEY_ASSISTANT, value).apply()

    /** "google", "bing", "duckduckgo" */
    var searchEngine: String
        get() = sp.getString(KEY_SEARCH, "google") ?: "google"
        set(value) = sp.edit().putString(KEY_SEARCH, value).apply()

    fun installedIds(): MutableSet<String> =
        HashSet(sp.getStringSet(KEY_INSTALLED, emptySet()) ?: emptySet())

    fun isInstalled(id: String): Boolean = installedIds().contains(id)

    fun install(id: String) {
        val set = installedIds().apply { add(id) }
        sp.edit().putStringSet(KEY_INSTALLED, set).apply()
    }

    fun uninstall(id: String) {
        val set = installedIds().apply { remove(id) }
        sp.edit().putStringSet(KEY_INSTALLED, set).apply()
    }

    /** Ensure system + default apps are marked installed on first boot. */
    fun ensureDefaults(defaultIds: List<String>) {
        if (sp.contains(KEY_INSTALLED)) return
        sp.edit().putStringSet(KEY_INSTALLED, defaultIds.toSet()).apply()
    }

    companion object {
        const val DEFAULT_LOCALE = "pt-BR"
        private const val KEY_SETUP = "setup_complete"
        private const val KEY_LOCALE = "locale"
        private const val KEY_ASSISTANT = "assistant"
        private const val KEY_SEARCH = "search_engine"
        private const val KEY_INSTALLED = "installed_ids"
    }
}
