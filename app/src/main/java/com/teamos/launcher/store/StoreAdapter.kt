package com.teamos.launcher.store

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teamos.launcher.core.Prefs
import com.teamos.launcher.data.AppEntry
import com.teamos.launcher.databinding.ItemStoreBinding
import com.teamos.launcher.i18n.Strings

class StoreAdapter(
    private var items: List<AppEntry>,
    private val prefs: Prefs,
    private val onOpen: (AppEntry) -> Unit,
    private val onChanged: (installed: Boolean) -> Unit
) : RecyclerView.Adapter<StoreAdapter.VH>() {

    class VH(val b: ItemStoreBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemStoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = items[position]
        val ctx = holder.b.root.context
        holder.b.icon.text = app.icon
        holder.b.name.text = app.name
        holder.b.publisher.text = app.publisher
        holder.b.desc.text = app.description

        val installed = prefs.isInstalled(app.id)
        holder.b.action.text = when {
            app.system || installed -> Strings.get(ctx, if (installed) "store.open" else "store.install")
            else -> Strings.get(ctx, "store.install")
        }

        holder.b.action.setOnClickListener {
            when {
                app.system -> onOpen(app)
                prefs.isInstalled(app.id) -> onOpen(app)
                else -> {
                    prefs.install(app.id)
                    notifyItemChanged(position)
                    onChanged(true)
                }
            }
        }

        holder.b.root.setOnClickListener { onOpen(app) }

        holder.b.root.setOnLongClickListener {
            if (!app.system && prefs.isInstalled(app.id)) {
                prefs.uninstall(app.id)
                notifyItemChanged(position)
                onChanged(false)
            }
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun submit(newItems: List<AppEntry>) {
        items = newItems
        notifyDataSetChanged()
    }
}
