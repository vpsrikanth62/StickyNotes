package com.example.stickynote

import android.appwidget.AppWidgetManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EditNoteActivity : AppCompatActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var rvNotes    : RecyclerView
    private lateinit var btnAdd     : Button
    private lateinit var btnSave    : Button
    private lateinit var btnCancel  : Button
    private lateinit var tvCount    : TextView
    private lateinit var adapter    : NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyBlurBehind()
        setContentView(R.layout.activity_edit_note)

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        bindViews()
        loadNotes()
    }

    private fun applyBlurBehind() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            val attrs = window.attributes
            attrs.blurBehindRadius = 48
            window.attributes = attrs
        }
    }

    private fun bindViews() {
        rvNotes   = findViewById(R.id.rv_notes)
        btnAdd    = findViewById(R.id.btn_add_item)
        btnSave   = findViewById(R.id.btn_save)
        btnCancel = findViewById(R.id.btn_cancel)
        tvCount   = findViewById(R.id.tv_char_count)

        adapter = NoteAdapter(mutableListOf()) { updateCount() }
        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.adapter = adapter

        btnAdd.setOnClickListener {
            adapter.addItem()
            rvNotes.smoothScrollToPosition(adapter.itemCount - 1)
        }
        btnSave.setOnClickListener   { saveNotes() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun loadNotes() {
        val saved = NoteRepository.load(this, widgetId).toMutableList()
        if (saved.isEmpty()) saved.add(NoteRepository.newItem())
        adapter = NoteAdapter(saved) { updateCount() }
        rvNotes.adapter = adapter
        updateCount()
    }

    private fun updateCount() {
        val items = adapter.getItems()
        val done  = items.count { it.isChecked }
        tvCount.text = "${done}/${items.size}"
    }

    private fun saveNotes() {
        val items = adapter.getItems().filter { it.text.isNotBlank() }
        NoteRepository.save(this, widgetId, items)
        val manager = AppWidgetManager.getInstance(this)
        manager.notifyAppWidgetViewDataChanged(widgetId, R.id.lv_notes)
        updateWidget(this, manager, widgetId)
        Toast.makeText(this, "Saved \u2728", Toast.LENGTH_SHORT).show()
        finish()
    }
}
