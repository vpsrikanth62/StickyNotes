package com.example.stickynote

import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

/** RecyclerView adapter for editing note items in EditNoteActivity. */
class NoteAdapter(
    private val items: MutableList<NoteItem>,
    private val onChanged: () -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteVH>() {

    inner class NoteVH(view: View) : RecyclerView.ViewHolder(view) {
        val ivCheck : ImageView   = view.findViewById(R.id.iv_check_editor)
        val etText  : EditText    = view.findViewById(R.id.et_item_text)
        val btnDel  : ImageButton = view.findViewById(R.id.btn_delete_item)

        // TextWatcher reference so we can remove it before rebind (prevents ghost updates)
        var textWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteVH =
        NoteVH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note_editor, parent, false)
        )

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: NoteVH, position: Int) {
        val item = items[position]

        // ── Checkbox state ────────────────────────────────────────────
        updateCheckUI(holder, item.isChecked)
        holder.ivCheck.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            val toggled = items[pos].copy(isChecked = !items[pos].isChecked)
            items[pos] = toggled
            updateCheckUI(holder, toggled.isChecked)
            onChanged()
        }

        // ── Text watcher: detach old, rebind new ──────────────────────
        holder.textWatcher?.let { holder.etText.removeTextChangedListener(it) }
        holder.etText.setText(item.text)
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    items[pos] = items[pos].copy(text = s?.toString() ?: "")
                    onChanged()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        }
        holder.textWatcher = watcher
        holder.etText.addTextChangedListener(watcher)

        // ── Delete ────────────────────────────────────────────────────
        holder.btnDel.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                items.removeAt(pos)
                notifyItemRemoved(pos)
                onChanged()
            }
        }
    }

    private fun updateCheckUI(holder: NoteVH, isChecked: Boolean) {
        holder.ivCheck.setImageResource(
            if (isChecked) R.drawable.ic_check_done else R.drawable.ic_check_empty
        )
        val flags = if (isChecked)
            Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
        else
            Paint.ANTI_ALIAS_FLAG
        holder.etText.paintFlags = flags
        holder.etText.alpha = if (isChecked) 0.45f else 1.0f
    }

    fun addItem() {
        items.add(NoteRepository.newItem())
        notifyItemInserted(items.size - 1)
        onChanged()
    }

    fun getItems(): List<NoteItem> = items.toList()
}
