package com.example.stickynote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PresetTaskAdapter(
    private val items: MutableList<PresetTask>
) : RecyclerView.Adapter<PresetTaskAdapter.PresetVH>() {

    inner class PresetVH(view: View) : RecyclerView.ViewHolder(view) {
        val cbEnabled: CheckBox = view.findViewById(R.id.cb_preset)
        val tvText: TextView = view.findViewById(R.id.tv_preset_text)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete_preset)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresetVH =
        PresetVH(LayoutInflater.from(parent.context).inflate(R.layout.item_preset_task, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PresetVH, position: Int) {
        val item = items[position]
        holder.tvText.text = item.text
        holder.cbEnabled.setOnCheckedChangeListener(null)
        holder.cbEnabled.isChecked = item.enabled
        holder.cbEnabled.setOnCheckedChangeListener { _, isChecked ->
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                items[pos] = items[pos].copy(enabled = isChecked)
            }
        }
        holder.btnDelete.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                items.removeAt(pos)
                notifyItemRemoved(pos)
            }
        }
    }

    fun addItem(text: String) {
        items.add(PresetTask(text = text, enabled = true))
        notifyItemInserted(items.size - 1)
    }

    fun getItems(): List<PresetTask> = items.toList()
}
