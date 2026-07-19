package com.teamos.launcher.apps

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.teamos.launcher.databinding.ActivityNotesBinding

/** Simple persistent notepad (single scratchpad note stored locally). */
class NotesActivity : AppCompatActivity() {

    private lateinit var b: ActivityNotesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(b.root)
        val sp = getSharedPreferences("teamos_notes", Context.MODE_PRIVATE)
        b.notes.setText(sp.getString("scratch", ""))
    }

    override fun onPause() {
        super.onPause()
        getSharedPreferences("teamos_notes", Context.MODE_PRIVATE)
            .edit().putString("scratch", b.notes.text.toString()).apply()
    }
}
