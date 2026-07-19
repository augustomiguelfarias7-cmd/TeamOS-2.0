package com.teamos.launcher.apps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teamos.launcher.databinding.ActivityPlaceholderBinding

/** Digital wellbeing scaffold: screen time + app limits land in Phase 3. */
class WellbeingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val b = ActivityPlaceholderBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.title.text = "Bem-estar Digital"
        b.body.text = "Tempo de uso, limites por app e resumos diários. Chega na Fase 3."
    }
}
