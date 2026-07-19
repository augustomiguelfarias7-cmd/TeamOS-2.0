package com.teamos.launcher.apps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teamos.launcher.databinding.ActivityPlaceholderBinding

/** Gallery scaffold: photos/videos + Team AI editing land in Phase 3/4. */
class GalleryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityPlaceholderBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.title.text = "Galeria"
        b.body.text = "Aqui ficarão fotos e vídeos do sistema, com edição via Team AI " +
            "(desenho no touch, criação/edição de imagens e melhoria de vídeo). Chega na Fase 3/4."
    }
}
