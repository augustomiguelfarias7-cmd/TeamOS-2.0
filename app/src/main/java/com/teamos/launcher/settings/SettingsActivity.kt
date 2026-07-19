package com.teamos.launcher.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.teamos.launcher.core.Navigator
import com.teamos.launcher.core.Prefs
import com.teamos.launcher.databinding.ActivitySettingsBinding
import com.teamos.launcher.i18n.Strings

class SettingsActivity : AppCompatActivity() {

    private lateinit var b: ActivitySettingsBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
        prefs = Prefs(this)

        applyStrings()

        b.rowLanguage.setOnClickListener { pickLanguage() }
        b.rowAssistant.setOnClickListener { pickAssistant() }
        b.rowSearch.setOnClickListener { pickSearch() }
        b.rowTeamAi.setOnClickListener { Navigator.openRoute(this, "teamos://team-ai") }
        b.rowWellbeing.setOnClickListener { Navigator.openRoute(this, "teamos://wellbeing") }
    }

    private fun applyStrings() {
        b.title.text = Strings.get(this, "settings.title")
        b.rowLanguageTitle.text = Strings.get(this, "settings.language")
        b.rowAssistantTitle.text = Strings.get(this, "settings.assistant")
        b.rowSearchTitle.text = Strings.get(this, "settings.search_engine")
        b.rowTeamAiTitle.text = Strings.get(this, "teamai.title")
        b.rowWellbeingTitle.text = Strings.get(this, "settings.wellbeing")

        b.rowLanguageValue.text = Strings.locales(this).firstOrNull { it.code == prefs.locale }?.name ?: prefs.locale
        b.rowAssistantValue.text =
            Strings.get(this, if (prefs.assistant == "gemini") "assistant.gemini" else "assistant.chatgpt")
        b.rowSearchValue.text = searchLabel(prefs.searchEngine)
    }

    private fun pickLanguage() {
        val locales = Strings.locales(this)
        val labels = locales.map { "${it.flag}  ${it.name}" }.toTypedArray()
        val current = locales.indexOfFirst { it.code == prefs.locale }
        AlertDialog.Builder(this)
            .setTitle(Strings.get(this, "settings.language"))
            .setSingleChoiceItems(labels, current) { d, which ->
                prefs.locale = locales[which].code
                applyStrings()
                d.dismiss()
            }
            .show()
    }

    private fun pickAssistant() {
        val values = listOf("chatgpt", "gemini")
        val labels = arrayOf(
            Strings.get(this, "assistant.chatgpt"),
            Strings.get(this, "assistant.gemini")
        )
        AlertDialog.Builder(this)
            .setTitle(Strings.get(this, "settings.assistant"))
            .setSingleChoiceItems(labels, values.indexOf(prefs.assistant)) { d, which ->
                prefs.assistant = values[which]
                applyStrings()
                d.dismiss()
            }
            .show()
    }

    private fun pickSearch() {
        val values = listOf("google", "bing", "duckduckgo")
        val labels = values.map { searchLabel(it) }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(Strings.get(this, "settings.search_engine"))
            .setSingleChoiceItems(labels, values.indexOf(prefs.searchEngine)) { d, which ->
                prefs.searchEngine = values[which]
                applyStrings()
                d.dismiss()
            }
            .show()
    }

    private fun searchLabel(id: String): String = when (id) {
        "bing" -> "Bing"
        "duckduckgo" -> "DuckDuckGo"
        else -> "Google"
    }
}
