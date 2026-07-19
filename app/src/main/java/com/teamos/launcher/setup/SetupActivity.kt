package com.teamos.launcher.setup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.teamos.launcher.MainActivity
import com.teamos.launcher.R
import com.teamos.launcher.core.Prefs
import com.teamos.launcher.databinding.ActivitySetupBinding
import com.teamos.launcher.i18n.LocaleInfo
import com.teamos.launcher.i18n.Strings

/** First-boot wizard (no login): Welcome -> Network -> Language -> Optional PIN -> Done. */
class SetupActivity : AppCompatActivity() {

    private lateinit var b: ActivitySetupBinding
    private lateinit var prefs: Prefs
    private var chosenLocale = Prefs.DEFAULT_LOCALE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(b.root)
        prefs = Prefs(this)
        chosenLocale = prefs.locale

        applyStrings()

        b.goButton.setOnClickListener { b.flipper.displayedChild = STEP_NETWORK }
        b.netOpenWifi.setOnClickListener { openWifiPanel() }
        b.netNext.setOnClickListener { b.flipper.displayedChild = STEP_LANGUAGE }

        buildLanguageList()
        b.langNext.setOnClickListener {
            prefs.locale = chosenLocale
            applyStrings()
            b.flipper.displayedChild = STEP_LOCK
        }

        b.lockNext.setOnClickListener { applyLockAndFinishStep() }
        b.finishButton.setOnClickListener { finishSetup() }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (b.flipper.displayedChild > 0) {
                    b.flipper.displayedChild -= 1
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun applyStrings() {
        b.welcomeTitle.text = Strings.forLocale(this, chosenLocale, "setup.welcome.subtitle")
        b.goButton.text = Strings.forLocale(this, chosenLocale, "setup.welcome.go")

        b.netTitle.text = Strings.forLocale(this, chosenLocale, "setup.network.title")
        b.netSubtitle.text = Strings.forLocale(this, chosenLocale, "setup.network.subtitle")
        b.netOpenWifi.text = Strings.forLocale(this, chosenLocale, "setup.network.open_wifi")
        b.netNext.text = Strings.forLocale(this, chosenLocale, "setup.next")

        b.langTitle.text = Strings.forLocale(this, chosenLocale, "setup.language.title")
        b.langSubtitle.text = Strings.forLocale(this, chosenLocale, "setup.language.subtitle")
        b.langNext.text = Strings.forLocale(this, chosenLocale, "setup.next")

        b.lockTitle.text = Strings.forLocale(this, chosenLocale, "setup.lock.title")
        b.lockSubtitle.text = Strings.forLocale(this, chosenLocale, "setup.lock.subtitle")
        b.pinField.hint = Strings.forLocale(this, chosenLocale, "setup.lock.pin_hint")
        b.pinConfirm.hint = Strings.forLocale(this, chosenLocale, "setup.lock.confirm_hint")
        b.lockNext.text = Strings.forLocale(this, chosenLocale, "setup.next")

        b.doneTitle.text = Strings.forLocale(this, chosenLocale, "setup.done.title")
        b.doneSubtitle.text = Strings.forLocale(this, chosenLocale, "setup.done.subtitle")
        b.finishButton.text = Strings.forLocale(this, chosenLocale, "setup.finish")
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

    private fun buildLanguageList() {
        b.langList.removeAllViews()
        for (loc in Strings.locales(this)) {
            b.langList.addView(localeRow(loc))
        }
    }

    private fun localeRow(loc: LocaleInfo): TextView {
        val row = TextView(this)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        lp.bottomMargin = dp(10)
        row.layoutParams = lp
        row.setPadding(dp(18), dp(16), dp(18), dp(16))
        row.textSize = 16f
        row.setBackgroundResource(R.drawable.bg_pill)
        row.setTextColor(ContextCompat.getColor(this, R.color.white))
        row.text = "${loc.flag}  ${loc.name}"
        row.tag = loc.code
        row.isSelected = loc.code == chosenLocale
        row.setOnClickListener {
            chosenLocale = loc.code
            prefs.locale = chosenLocale
            for (i in 0 until b.langList.childCount) {
                val child = b.langList.getChildAt(i)
                child.isSelected = child.tag == loc.code
            }
            applyStrings()
        }
        return row
    }

    private fun applyLockAndFinishStep() {
        val pin = b.pinField.text.toString()
        val confirm = b.pinConfirm.text.toString()
        when {
            pin.isEmpty() -> prefs.clearLock()
            pin.length < 4 -> {
                Toast.makeText(this, Strings.forLocale(this, chosenLocale, "setup.lock.too_short"), Toast.LENGTH_SHORT).show()
                return
            }
            pin != confirm -> {
                Toast.makeText(this, Strings.forLocale(this, chosenLocale, "setup.lock.mismatch"), Toast.LENGTH_SHORT).show()
                return
            }
            else -> prefs.setPin(pin)
        }
        b.flipper.displayedChild = STEP_DONE
    }

    private fun finishSetup() {
        prefs.locale = chosenLocale
        prefs.setupComplete = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    companion object {
        private const val STEP_NETWORK = 1
        private const val STEP_LANGUAGE = 2
        private const val STEP_LOCK = 3
        private const val STEP_DONE = 4
    }
}
