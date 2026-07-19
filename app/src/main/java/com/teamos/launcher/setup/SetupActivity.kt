package com.teamos.launcher.setup

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.teamos.launcher.MainActivity
import com.teamos.launcher.R
import com.teamos.launcher.core.Prefs
import com.teamos.launcher.databinding.ActivitySetupBinding
import com.teamos.launcher.i18n.LocaleInfo
import com.teamos.launcher.i18n.Strings

/** First-boot wizard (no login): Welcome -> Language -> Assistant -> Done. */
class SetupActivity : AppCompatActivity() {

    private lateinit var b: ActivitySetupBinding
    private lateinit var prefs: Prefs
    private var chosenLocale = Prefs.DEFAULT_LOCALE
    private var chosenAssistant = "chatgpt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(b.root)
        prefs = Prefs(this)
        chosenLocale = prefs.locale

        applyWelcomeStrings()
        b.goButton.setOnClickListener { goToLanguage() }

        buildLanguageList()
        b.langNext.setOnClickListener { goToAssistant() }

        b.optChatgpt.setOnClickListener { selectAssistant("chatgpt") }
        b.optGemini.setOnClickListener { selectAssistant("gemini") }
        selectAssistant(chosenAssistant)

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

    private fun applyWelcomeStrings() {
        b.welcomeTitle.text = Strings.forLocale(this, chosenLocale, "setup.welcome.title")
        b.welcomeSubtitle.text = Strings.forLocale(this, chosenLocale, "setup.welcome.subtitle")
        b.goButton.text = Strings.forLocale(this, chosenLocale, "setup.welcome.go")
    }

    private fun buildLanguageList() {
        b.langList.removeAllViews()
        for (loc in Strings.locales(this)) {
            b.langList.addView(localeRow(loc))
        }
        refreshLanguageStrings()
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
            for (i in 0 until b.langList.childCount) {
                val child = b.langList.getChildAt(i)
                child.isSelected = child.tag == loc.code
            }
            refreshLanguageStrings()
        }
        return row
    }

    private fun refreshLanguageStrings() {
        b.langTitle.text = Strings.forLocale(this, chosenLocale, "setup.language.title")
        b.langSubtitle.text = Strings.forLocale(this, chosenLocale, "setup.language.subtitle")
        b.langNext.text = Strings.forLocale(this, chosenLocale, "setup.next")
    }

    private fun selectAssistant(which: String) {
        chosenAssistant = which
        b.optChatgpt.isSelected = which == "chatgpt"
        b.optGemini.isSelected = which == "gemini"
        b.asstTitle.text = Strings.forLocale(this, chosenLocale, "setup.assistant.title")
        b.asstSubtitle.text = Strings.forLocale(this, chosenLocale, "setup.assistant.subtitle")
        b.finishButton.text = Strings.forLocale(this, chosenLocale, "setup.finish")
    }

    private fun goToLanguage() {
        refreshLanguageStrings()
        b.flipper.displayedChild = 1
    }

    private fun goToAssistant() {
        prefs.locale = chosenLocale
        selectAssistant(chosenAssistant)
        b.flipper.displayedChild = 2
    }

    private fun finishSetup() {
        prefs.locale = chosenLocale
        prefs.assistant = chosenAssistant
        prefs.setupComplete = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
