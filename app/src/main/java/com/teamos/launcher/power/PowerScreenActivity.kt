package com.teamos.launcher.power

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teamos.launcher.databinding.ActivityPowerScreenBinding
import com.teamos.launcher.i18n.Strings

/** Black "powering off / restarting" screen. Simulated only — tap to return. */
class PowerScreenActivity : AppCompatActivity() {

    private lateinit var b: ActivityPowerScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityPowerScreenBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.msg.text = intent.getStringExtra(EXTRA_MSG) ?: Strings.get(this, "power.shutting_down")
        b.hint.text = Strings.get(this, "power.sim_hint")
        b.root.setOnClickListener { finish() }
    }

    companion object {
        const val EXTRA_MSG = "msg"
    }
}
