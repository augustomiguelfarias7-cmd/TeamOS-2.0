package com.teamos.launcher.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.teamos.launcher.apps.SimActivity
import com.teamos.launcher.apps.SosActivity
import com.teamos.launcher.core.Navigator
import com.teamos.launcher.core.Prefs
import com.teamos.launcher.core.Wallpaper
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

        b.rowWifi.setOnClickListener { openWifiPanel() }
        b.rowSim.setOnClickListener { startActivity(Intent(this, SimActivity::class.java)) }
        b.rowWallpaper.setOnClickListener { pickWallpaper() }
        b.rowLanguage.setOnClickListener { pickLanguage() }
        b.rowAssistant.setOnClickListener { pickAssistant() }
        b.rowSearch.setOnClickListener { pickSearch() }
        b.rowLock.setOnClickListener { configureLock() }
        b.rowSos.setOnClickListener { startActivity(Intent(this, SosActivity::class.java)) }
        b.rowTeamAi.setOnClickListener { Navigator.openRoute(this, "teamos://team-ai") }
        b.rowWellbeing.setOnClickListener { Navigator.openRoute(this, "teamos://wellbeing") }
    }

    override fun onResume() {
        super.onResume()
        applyStrings()
    }

    private fun applyStrings() {
        b.title.text = Strings.get(this, "settings.title")
        b.secConnections.text = Strings.get(this, "settings.sec.connections")
        b.secPersonal.text = Strings.get(this, "settings.sec.personal")
        b.secAssistant.text = Strings.get(this, "settings.sec.assistant")
        b.secSecurity.text = Strings.get(this, "settings.sec.security")
        b.secAi.text = Strings.get(this, "settings.sec.ai")

        b.rowWifiTitle.text = Strings.get(this, "settings.wifi")
        b.rowSimTitle.text = Strings.get(this, "sim.title")
        b.rowWallpaperTitle.text = Strings.get(this, "settings.wallpaper")
        b.rowLanguageTitle.text = Strings.get(this, "settings.language")
        b.rowAssistantTitle.text = Strings.get(this, "settings.assistant")
        b.rowSearchTitle.text = Strings.get(this, "settings.search_engine")
        b.rowLockTitle.text = Strings.get(this, "settings.lock")
        b.rowSosTitle.text = Strings.get(this, "sos.title")
        b.rowTeamAiTitle.text = Strings.get(this, "teamai.title")
        b.rowWellbeingTitle.text = Strings.get(this, "settings.wellbeing")

        b.rowWallpaperValue.text = Strings.get(
            this, if (prefs.wallpaperMode == "photo") "wallpaper.photo" else "wallpaper.default"
        )
        b.rowLanguageValue.text = Strings.locales(this).firstOrNull { it.code == prefs.locale }?.name ?: prefs.locale
        b.rowAssistantValue.text =
            Strings.get(this, if (prefs.assistant == "gemini") "assistant.gemini" else "assistant.chatgpt")
        b.rowSearchValue.text = searchLabel(prefs.searchEngine)
        b.rowLockValue.text = Strings.get(this, if (prefs.lockEnabled) "lock.on" else "lock.off")
    }

    private fun openWifiPanel() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent(Settings.Panel.ACTION_WIFI)
        } else {
            Intent(Settings.ACTION_WIFI_SETTINGS)
        }
        runCatching { startActivity(intent) }
            .onFailure { startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }
    }

    private fun pickWallpaper() {
        val labels = arrayOf(
            Strings.get(this, "wallpaper.default"),
            Strings.get(this, "wallpaper.photo")
        )
        AlertDialog.Builder(this)
            .setTitle(Strings.get(this, "settings.wallpaper"))
            .setItems(labels) { _, which ->
                if (which == 0) {
                    Wallpaper.useDefault(this)
                    applyStrings()
                } else {
                    Toast.makeText(this, Strings.get(this, "wallpaper.loading"), Toast.LENGTH_SHORT).show()
                    Wallpaper.shuffle(this) {
                        applyStrings()
                        Toast.makeText(this, Strings.get(this, "wallpaper.ready"), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
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

    private fun configureLock() {
        if (prefs.lockEnabled) {
            AlertDialog.Builder(this)
                .setTitle(Strings.get(this, "settings.lock"))
                .setItems(
                    arrayOf(
                        Strings.get(this, "lock.change"),
                        Strings.get(this, "lock.remove")
                    )
                ) { _, which ->
                    if (which == 0) promptNewPin() else {
                        prefs.clearLock()
                        applyStrings()
                    }
                }
                .show()
        } else {
            promptNewPin()
        }
    }

    private fun promptNewPin() {
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = Strings.get(this@SettingsActivity, "setup.lock.pin_hint")
        }
        AlertDialog.Builder(this)
            .setTitle(Strings.get(this, "settings.lock"))
            .setView(input)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val pin = input.text.toString()
                if (pin.length < 4) {
                    Toast.makeText(this, Strings.get(this, "setup.lock.too_short"), Toast.LENGTH_SHORT).show()
                } else {
                    prefs.setPin(pin)
                    applyStrings()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun searchLabel(id: String): String = when (id) {
        "bing" -> "Bing"
        "duckduckgo" -> "DuckDuckGo"
        else -> "Google"
    }
}
