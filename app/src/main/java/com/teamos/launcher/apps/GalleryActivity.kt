package com.teamos.launcher.apps

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.teamos.launcher.databinding.ActivityGalleryBinding
import com.teamos.launcher.i18n.Strings
import com.teamos.launcher.teamai.TeamAiEditorActivity

/** Gallery with integrated Team AI editing (image generation + touch drawing). */
class GalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.title.text = Strings.get(this, "gallery.title")
        b.subtitle.text = Strings.get(this, "gallery.subtitle")
        b.rowAiEditorTitle.text = Strings.get(this, "teamai.edit_image")
        b.rowAiEditorValue.text = Strings.get(this, "teamai.edit_image.desc")
        b.rowEnhanceTitle.text = Strings.get(this, "teamai.enhance_video")
        b.rowEnhanceValue.text = Strings.get(this, "teamai.enhance_video.desc")

        b.rowAiEditor.setOnClickListener {
            startActivity(Intent(this, TeamAiEditorActivity::class.java))
        }
        b.rowEnhance.setOnClickListener {
            Toast.makeText(this, Strings.get(this, "common.soon"), Toast.LENGTH_SHORT).show()
        }
    }
}
