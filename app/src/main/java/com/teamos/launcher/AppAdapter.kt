package com.teamos.launcher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.teamos.launcher.data.AppEntry
import com.teamos.launcher.databinding.ItemAppBinding

/** Grid adapter for launcher home / all-apps drawer. */
class AppAdapter(
    private var items: List<AppEntry>,
    private val onClick: (AppEntry) -> Unit,
    private val onLongClick: (AppEntry) -> Unit = {}
) : RecyclerView.Adapter<AppAdapter.VH>() {

    class VH(val b: ItemAppBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = items[position]
        holder.b.icon.text = app.icon
        holder.b.label.text = app.name
        holder.b.root.setOnClickListener { onClick(app) }
        holder.b.root.setOnLongClickListener { onLongClick(app); true }
    }

    override fun getItemCount(): Int = items.size

    fun submit(newItems: List<AppEntry>) {
        items = newItems
        notifyDataSetChanged()
    }
}
