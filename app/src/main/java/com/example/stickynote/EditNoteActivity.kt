package com.example.stickynote

import android.appwidget.AppWidgetManager
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EditNoteActivity : AppCompatActivity() {
    companion object {
        private const val MENU_FONT_MONTSERRAT = 1
        private const val MENU_FONT_SYSTEM = 2
        private const val MENU_FONT_SERIF = 3
        private const val MENU_FONT_MONO = 4
        private const val MENU_FONT_MEDIUM = 5
        private const val MENU_FONT_CONDENSED = 6

        private const val MENU_ACCENT_VIOLET = 20
        private const val MENU_ACCENT_CYAN = 21
        private const val MENU_ACCENT_ROSE = 22
        private const val MENU_ACCENT_EMERALD = 23
        private const val MENU_ACCENT_AMBER = 24
        private const val MENU_ACCENT_INDIGO = 25
        private const val MENU_ACCENT_CRIMSON = 26
        private const val MENU_ACCENT_TEAL = 27
    }

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var fontMode = UiStylePrefs.FONT_MONTSERRAT
    private var accentMode = UiStylePrefs.ACCENT_VIOLET

    private lateinit var rvNotes    : RecyclerView
    private lateinit var btnAdd     : Button
    private lateinit var btnSave    : Button
    private lateinit var btnCancel  : Button
    private lateinit var tvCount    : TextView
    private lateinit var btnSettings: ImageButton
    private lateinit var adapter    : NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        loadUiPrefs()
        applyEditorStyle()
        loadNotes()
    }

    private fun bindViews() {
        rvNotes   = findViewById(R.id.rv_notes)
        btnAdd    = findViewById(R.id.btn_add_item)
        btnSave   = findViewById(R.id.btn_save)
        btnCancel = findViewById(R.id.btn_cancel)
        tvCount   = findViewById(R.id.tv_char_count)
        btnSettings = findViewById(R.id.btn_settings)

        adapter = NoteAdapter(mutableListOf()) { updateCount() }
        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.adapter = adapter

        btnAdd.setOnClickListener {
            val insertAt = adapter.addItem()
            rvNotes.smoothScrollToPosition(insertAt)
            rvNotes.post {
                val vh = rvNotes.findViewHolderForAdapterPosition(insertAt) as? NoteAdapter.NoteVH
                val et = vh?.etText
                if (et != null) {
                    et.requestFocus()
                    et.setSelection(et.text?.length ?: 0)
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
        btnSave.setOnClickListener   { saveNotes() }
        btnCancel.setOnClickListener { finish() }
        btnSettings.setOnClickListener { showSettingsMenu() }
    }

    private fun showSettingsMenu() {
        PopupMenu(this, btnSettings).apply {
            addSection(menu, getString(R.string.menu_fonts))
            addOption(menu, MENU_FONT_MONTSERRAT, getString(R.string.font_montserrat), fontMode == UiStylePrefs.FONT_MONTSERRAT)
            addOption(menu, MENU_FONT_SYSTEM, getString(R.string.font_system), fontMode == UiStylePrefs.FONT_SYSTEM)
            addOption(menu, MENU_FONT_SERIF, getString(R.string.font_serif), fontMode == UiStylePrefs.FONT_SERIF)
            addOption(menu, MENU_FONT_MONO, getString(R.string.font_mono), fontMode == UiStylePrefs.FONT_MONO)
            addOption(menu, MENU_FONT_MEDIUM, getString(R.string.font_medium), fontMode == UiStylePrefs.FONT_MEDIUM)
            addOption(menu, MENU_FONT_CONDENSED, getString(R.string.font_condensed), fontMode == UiStylePrefs.FONT_CONDENSED)

            addSection(menu, getString(R.string.menu_accents))
            addOption(menu, MENU_ACCENT_VIOLET, getString(R.string.accent_violet), accentMode == UiStylePrefs.ACCENT_VIOLET)
            addOption(menu, MENU_ACCENT_CYAN, getString(R.string.accent_cyan), accentMode == UiStylePrefs.ACCENT_CYAN)
            addOption(menu, MENU_ACCENT_ROSE, getString(R.string.accent_rose), accentMode == UiStylePrefs.ACCENT_ROSE)
            addOption(menu, MENU_ACCENT_EMERALD, getString(R.string.accent_emerald), accentMode == UiStylePrefs.ACCENT_EMERALD)
            addOption(menu, MENU_ACCENT_AMBER, getString(R.string.accent_amber), accentMode == UiStylePrefs.ACCENT_AMBER)
            addOption(menu, MENU_ACCENT_INDIGO, getString(R.string.accent_indigo), accentMode == UiStylePrefs.ACCENT_INDIGO)
            addOption(menu, MENU_ACCENT_CRIMSON, getString(R.string.accent_crimson), accentMode == UiStylePrefs.ACCENT_CRIMSON)
            addOption(menu, MENU_ACCENT_TEAL, getString(R.string.accent_teal), accentMode == UiStylePrefs.ACCENT_TEAL)

            setOnMenuItemClickListener { item -> onSettingsItemSelected(item) }
            show()
        }
    }

    private fun addSection(menu: Menu, title: String) {
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, title).isEnabled = false
    }

    private fun addOption(menu: Menu, id: Int, label: String, selected: Boolean) {
        val prefix = if (selected) "✓ " else "  "
        menu.add(Menu.NONE, id, Menu.NONE, prefix + label)
    }

    private fun onSettingsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            MENU_FONT_MONTSERRAT -> fontMode = UiStylePrefs.FONT_MONTSERRAT
            MENU_FONT_SYSTEM -> fontMode = UiStylePrefs.FONT_SYSTEM
            MENU_FONT_SERIF -> fontMode = UiStylePrefs.FONT_SERIF
            MENU_FONT_MONO -> fontMode = UiStylePrefs.FONT_MONO
            MENU_FONT_MEDIUM -> fontMode = UiStylePrefs.FONT_MEDIUM
            MENU_FONT_CONDENSED -> fontMode = UiStylePrefs.FONT_CONDENSED
            MENU_ACCENT_VIOLET -> accentMode = UiStylePrefs.ACCENT_VIOLET
            MENU_ACCENT_CYAN -> accentMode = UiStylePrefs.ACCENT_CYAN
            MENU_ACCENT_ROSE -> accentMode = UiStylePrefs.ACCENT_ROSE
            MENU_ACCENT_EMERALD -> accentMode = UiStylePrefs.ACCENT_EMERALD
            MENU_ACCENT_AMBER -> accentMode = UiStylePrefs.ACCENT_AMBER
            MENU_ACCENT_INDIGO -> accentMode = UiStylePrefs.ACCENT_INDIGO
            MENU_ACCENT_CRIMSON -> accentMode = UiStylePrefs.ACCENT_CRIMSON
            MENU_ACCENT_TEAL -> accentMode = UiStylePrefs.ACCENT_TEAL
            else -> return false
        }
        saveUiPrefs()
        applyEditorStyle()
        return true
    }

    private fun loadUiPrefs() {
        fontMode = UiStylePrefs.fontMode(this)
        accentMode = UiStylePrefs.accentMode(this)
    }

    private fun saveUiPrefs() {
        getSharedPreferences(UiStylePrefs.PREFS, MODE_PRIVATE).edit()
            .putString(UiStylePrefs.KEY_FONT_MODE, fontMode)
            .putString(UiStylePrefs.KEY_ACCENT_MODE, accentMode)
            .apply()
    }

    private fun applyEditorStyle() {
        val typeface = when (fontMode) {
            UiStylePrefs.FONT_SYSTEM -> Typeface.SANS_SERIF
            UiStylePrefs.FONT_SERIF -> Typeface.SERIF
            UiStylePrefs.FONT_MONO -> Typeface.MONOSPACE
            UiStylePrefs.FONT_MEDIUM -> Typeface.create("sans-serif-medium", Typeface.NORMAL)
            UiStylePrefs.FONT_CONDENSED -> Typeface.create("sans-serif-condensed", Typeface.NORMAL)
            else -> Typeface.create("montserrat", Typeface.NORMAL)
        }
        applyTypefaceRecursive(findViewById(android.R.id.content), typeface)
        adapter.setFontMode(fontMode)
        applyAccentGradient()
    }

    private fun applyTypefaceRecursive(view: View, tf: Typeface) {
        if (view is TextView) view.typeface = tf
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) applyTypefaceRecursive(view.getChildAt(i), tf)
        }
    }

    private fun applyAccentGradient() {
        val (start, end) = UiStylePrefs.accentGradient(accentMode)
        val saveBg = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(start, end)
        ).apply { cornerRadius = 50f * resources.displayMetrics.density }
        btnSave.background = saveBg
        btnAdd.setTextColor(start)
    }

    private fun loadNotes() {
        val saved = NoteRepository.load(this, widgetId).toMutableList()
        if (saved.isEmpty()) saved.add(NoteRepository.newItem())
        adapter = NoteAdapter(saved) { updateCount() }
        adapter.setFontMode(fontMode)
        rvNotes.adapter = adapter
        updateCount()
    }

    private fun updateCount() {
        val items = adapter.getItems()
        val done  = items.count { it.isChecked }
        tvCount.text = "${done}/${items.size}"
    }

    private fun saveNotes() {
        val items = adapter.getItems()
            .filter { it.text.isNotBlank() }
            .sortedBy { it.isChecked }
        NoteRepository.save(this, widgetId, items)
        val manager = AppWidgetManager.getInstance(this)
        manager.notifyAppWidgetViewDataChanged(widgetId, R.id.lv_notes)
        updateWidget(this, manager, widgetId)
        Toast.makeText(this, "Saved \u2728", Toast.LENGTH_SHORT).show()
        finish()
    }
}
