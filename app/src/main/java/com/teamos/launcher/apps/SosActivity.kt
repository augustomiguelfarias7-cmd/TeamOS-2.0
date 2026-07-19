package com.teamos.launcher.apps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.teamos.launcher.core.Prefs
import com.teamos.launcher.databinding.ActivitySosBinding
import com.teamos.launcher.i18n.Strings

/**
 * SOS: opens the messaging app pre-filled with an emergency message to a saved
 * contact. The user confirms the send — the launcher never sends SMS silently.
 */
class SosActivity : AppCompatActivity() {

    private lateinit var b: ActivitySosBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySosBinding.inflate(layoutInflater)
        setContentView(b.root)
        prefs = Prefs(this)

        b.title.text = Strings.get(this, "sos.title")
        b.subtitle.text = Strings.get(this, "sos.subtitle")
        b.contact.hint = Strings.get(this, "sos.contact_hint")
        b.message.hint = Strings.get(this, "sos.message_hint")
        b.send.text = Strings.get(this, "sos.send")
        b.note.text = Strings.get(this, "sos.note")

        b.contact.setText(prefs.sosContact)
        b.message.setText(prefs.sosMessage.ifBlank { Strings.get(this, "sos.default_message") })

        b.send.setOnClickListener { sendSos() }
    }

    private fun sendSos() {
        val number = b.contact.text.toString().trim()
        val text = b.message.text.toString().trim()
        if (number.isEmpty()) {
            Toast.makeText(this, Strings.get(this, "sos.need_contact"), Toast.LENGTH_SHORT).show()
            return
        }
        prefs.sosContact = number
        prefs.sosMessage = text

        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number"))
            .putExtra("sms_body", text)
        runCatching { startActivity(intent) }
            .onFailure { Toast.makeText(this, Strings.get(this, "sos.no_sms"), Toast.LENGTH_SHORT).show() }
    }
}
