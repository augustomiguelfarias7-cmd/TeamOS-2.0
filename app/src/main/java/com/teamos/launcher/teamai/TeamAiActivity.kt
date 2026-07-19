package com.teamos.launcher.teamai

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.teamos.launcher.databinding.ActivityTeamaiBinding
import com.teamos.launcher.i18n.Strings

/**
 * Team AI hub. Feature engines (image editor, video upscaler, live captions)
 * land in Phase 4 (JS/Puter.js + on-device models). This screen wires the entry
 * points and localized labels.
 */
class TeamAiActivity : AppCompatActivity() {

    private lateinit var b: ActivityTeamaiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityTeamaiBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.title.text = Strings.get(this, "teamai.title")
        b.rowEditImageTitle.text = Strings.get(this, "teamai.edit_image")
        b.rowEnhanceVideoTitle.text = Strings.get(this, "teamai.enhance_video")
        b.rowCaptionsTitle.text = Strings.get(this, "teamai.live_captions")

        b.rowEditImage.setOnClickListener {
            startActivity(Intent(this, TeamAiEditorActivity::class.java))
        }
        b.rowEnhanceVideo.setOnClickListener { soon() }
        b.rowCaptions.setOnClickListener { soon() }
    }

    private fun soon() {
        Toast.makeText(this, Strings.get(this, "common.soon"), Toast.LENGTH_SHORT).show()
    }
}
