package com.teamos.launcher.apps

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.teamos.launcher.databinding.ActivitySimBinding
import com.teamos.launcher.i18n.Strings

/** SIM / mobile network info. Reads carrier state via TelephonyManager when permitted. */
class SimActivity : AppCompatActivity() {

    private lateinit var b: ActivitySimBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySimBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.title.text = Strings.get(this, "sim.title")
        b.rowCarrierTitle.text = Strings.get(this, "sim.carrier")
        b.rowStateTitle.text = Strings.get(this, "sim.state")
        b.rowNumberTitle.text = Strings.get(this, "sim.number")
        b.note.text = Strings.get(this, "sim.note")
        b.grant.text = Strings.get(this, "sim.grant")

        b.grant.setOnClickListener {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), 1)
        }
        refresh()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        refresh()
    }

    private fun refresh() {
        val tm = getSystemService(TELEPHONY_SERVICE) as? TelephonyManager
        if (tm == null) {
            b.rowStateValue.text = Strings.get(this, "sim.unavailable")
            return
        }
        b.rowStateValue.text = stateLabel(tm.simState)
        b.rowCarrierValue.text = tm.networkOperatorName.ifBlank { "—" }

        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
        b.grant.visibility = if (granted) android.view.View.GONE else android.view.View.VISIBLE
        b.rowNumberValue.text = if (granted) {
            runCatching {
                @Suppress("DEPRECATION")
                tm.line1Number
            }.getOrNull()?.ifBlank { "—" } ?: "—"
        } else {
            Strings.get(this, "sim.need_permission")
        }
    }

    private fun stateLabel(state: Int): String = when (state) {
        TelephonyManager.SIM_STATE_READY -> Strings.get(this, "sim.ready")
        TelephonyManager.SIM_STATE_ABSENT -> Strings.get(this, "sim.absent")
        TelephonyManager.SIM_STATE_PIN_REQUIRED,
        TelephonyManager.SIM_STATE_PUK_REQUIRED,
        TelephonyManager.SIM_STATE_NETWORK_LOCKED -> Strings.get(this, "sim.locked")
        else -> Strings.get(this, "sim.unavailable")
    }
}
