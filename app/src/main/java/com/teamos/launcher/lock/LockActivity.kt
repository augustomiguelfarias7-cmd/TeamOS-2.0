package com.teamos.launcher.lock

import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.teamos.launcher.core.Prefs
import com.teamos.launcher.databinding.ActivityLockBinding
import com.teamos.launcher.i18n.Strings

/**
 * App-level lock screen for the TeamOS launcher. This is a shell-level lock, not a
 * replacement for the secure Android keyguard.
 */
class LockActivity : AppCompatActivity() {

    private lateinit var b: ActivityLockBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLockBinding.inflate(layoutInflater)
        setContentView(b.root)
        prefs = Prefs(this)

        b.lockTitle.text = Strings.get(this, "lock.title")
        b.unlockButton.text = Strings.get(this, "lock.unlock")
        b.unlockButton.setOnClickListener { tryUnlock() }

        // Block back: the lock must be satisfied.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })
    }

    private fun tryUnlock() {
        if (prefs.checkPin(b.pinField.text.toString())) {
            Session.unlocked = true
            finish()
        } else {
            b.pinField.text?.clear()
            Toast.makeText(this, Strings.get(this, "lock.wrong"), Toast.LENGTH_SHORT).show()
        }
    }

    /** In-memory unlock state for the current launcher session. */
    object Session {
        var unlocked = false
    }
}
