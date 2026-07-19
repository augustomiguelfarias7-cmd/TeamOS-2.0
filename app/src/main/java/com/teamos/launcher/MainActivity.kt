package com.teamos.launcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.teamos.launcher.assistant.AssistantOverlayActivity
import com.teamos.launcher.core.Navigator
import com.teamos.launcher.core.Prefs
import com.teamos.launcher.core.Wallpaper
import com.teamos.launcher.data.AppCatalog
import com.teamos.launcher.databinding.ActivityMainBinding
import com.teamos.launcher.i18n.Strings
import com.teamos.launcher.lock.LockActivity
import com.teamos.launcher.power.PowerMenuActivity
import com.teamos.launcher.setup.SetupActivity

/** TeamOS launcher home screen. Acts as the device HOME. */
class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var prefs: Prefs
    private lateinit var adapter: AppAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        prefs.ensureDefaults(AppCatalog.defaultInstalledIds(this))

        if (!prefs.setupComplete) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        adapter = AppAdapter(installedApps(), onClick = { Navigator.open(this, it) })
        b.appGrid.layoutManager = GridLayoutManager(this, 4)
        b.appGrid.adapter = adapter

        b.searchBar.setOnClickListener { Navigator.openRoute(this, "teamos://browser") }
        b.dockStore.setOnClickListener { Navigator.openRoute(this, "teamos://store") }
        b.dockBrowser.setOnClickListener { Navigator.openRoute(this, "teamos://browser") }
        b.dockSettings.setOnClickListener { Navigator.openRoute(this, "teamos://settings") }
        b.dockAssistant.setOnClickListener {
            startActivity(Intent(this, AssistantOverlayActivity::class.java))
        }

        // Long-press the home surface to open the power menu (hold-home gesture).
        val openPower = {
            startActivity(Intent(this, PowerMenuActivity::class.java)); true
        }
        b.homeRoot.setOnLongClickListener { openPower() }
        b.appGrid.setOnLongClickListener { openPower() }
    }

    override fun onResume() {
        super.onResume()
        if (::b.isInitialized) {
            maybeLock()
            Wallpaper.apply(this, b.wallpaper)
            b.searchHint.text = Strings.get(this, "home.search_hint")
            adapter.submit(installedApps())
        }
    }

    private fun maybeLock() {
        if (prefs.lockEnabled && !LockActivity.Session.unlocked) {
            startActivity(Intent(this, LockActivity::class.java))
        }
    }

    private fun installedApps() =
        AppCatalog.apps(this).filter { prefs.isInstalled(it.id) }
}
