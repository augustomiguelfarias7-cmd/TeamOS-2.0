package com.teamos.launcher.apps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teamos.launcher.databinding.ActivityGalleryBinding
import com.teamos.launcher.i18n.Strings

/** System gallery for the device's photos and videos. */
class GalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityGalleryBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.title.text = Strings.get(this, "gallery.title")
        b.subtitle.text = Strings.get(this, "gallery.subtitle")
        b.empty.text = Strings.get(this, "gallery.empty")
    }
}
