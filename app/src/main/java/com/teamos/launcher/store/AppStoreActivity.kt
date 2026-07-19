package com.teamos.launcher.store

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.teamos.launcher.core.Navigator
import com.teamos.launcher.core.Prefs
import com.teamos.launcher.data.AppCatalog
import com.teamos.launcher.data.AppEntry
import com.teamos.launcher.databinding.ActivityStoreBinding
import com.teamos.launcher.i18n.Strings

class AppStoreActivity : AppCompatActivity() {

    private lateinit var b: ActivityStoreBinding
    private lateinit var prefs: Prefs
    private lateinit var adapter: StoreAdapter
    private lateinit var all: List<AppEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityStoreBinding.inflate(layoutInflater)
        setContentView(b.root)
        prefs = Prefs(this)

        b.title.text = Strings.get(this, "store.title")
        all = AppCatalog.apps(this).filter { !it.system }

        adapter = StoreAdapter(
            all, prefs,
            onOpen = { Navigator.open(this, it) },
            onChanged = {
                Toast.makeText(this, Strings.get(this, "store.installed"), Toast.LENGTH_SHORT).show()
            }
        )
        b.list.layoutManager = LinearLayoutManager(this)
        b.list.adapter = adapter

        b.search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = filter(s?.toString().orEmpty())
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })
    }

    private fun filter(query: String) {
        val q = query.trim().lowercase()
        adapter.submit(
            if (q.isEmpty()) all
            else all.filter {
                it.name.lowercase().contains(q) || it.publisher.lowercase().contains(q)
            }
        )
    }
}
