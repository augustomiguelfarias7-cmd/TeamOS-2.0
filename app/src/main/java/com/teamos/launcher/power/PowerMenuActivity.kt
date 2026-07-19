package com.teamos.launcher.power

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teamos.launcher.MainActivity
import com.teamos.launcher.databinding.ActivityPowerBinding
import com.teamos.launcher.i18n.Strings
import com.teamos.launcher.lock.LockActivity

/**
 * Power menu overlay (opened by long-pressing the home area). "Lock" is real
 * (shell-level). "Restart"/"Shutdown" only show the UI — a normal launcher cannot
 * power the device off; that needs a system build.
 */
class PowerMenuActivity : AppCompatActivity() {

    private lateinit var b: ActivityPowerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPowerBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.powerTitle.text = Strings.get(this, "power.title")
        b.powerLock.text = Strings.get(this, "power.lock")
        b.powerRestart.text = Strings.get(this, "power.restart")
        b.powerShutdown.text = Strings.get(this, "power.shutdown")
        b.powerNote.text = Strings.get(this, "power.note")

        b.powerLock.setOnClickListener { lockNow() }
        b.powerRestart.setOnClickListener { simulate("power.restarting") }
        b.powerShutdown.setOnClickListener { simulate("power.shutting_down") }
        b.powerRoot.setOnClickListener { finish() }
    }

    private fun lockNow() {
        LockActivity.Session.unlocked = false
        val i = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(i)
        finish()
    }

    private fun simulate(key: String) {
        val i = Intent(this, PowerScreenActivity::class.java)
            .putExtra(PowerScreenActivity.EXTRA_MSG, Strings.get(this, key))
        startActivity(i)
        finish()
    }
}
