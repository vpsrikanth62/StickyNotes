package com.example.stickynote

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditNoteActivity : AppCompatActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var etNote: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var tvCharCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        // Keep keyboard open immediately
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        etNote = findViewById(R.id.et_note)
        btnSave = findViewById(R.id.btn_save)
        btnCancel = findViewById(R.id.btn_cancel)
        tvCharCount = findViewById(R.id.tv_char_count)

        // Load existing note
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedNote = prefs.getString("$PREF_KEY_PREFIX$widgetId", "") ?: ""
        etNote.setText(savedNote)
        etNote.setSelection(savedNote.length) // move cursor to end
        updateCharCount(savedNote.length)

        etNote.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                updateCharCount(s?.length ?: 0)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnSave.setOnClickListener { saveNote() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun updateCharCount(length: Int) {
        tvCharCount.text = "$length chars"
    }

    private fun saveNote() {
        val noteText = etNote.text.toString()

        // Persist note
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("$PREF_KEY_PREFIX$widgetId", noteText).apply()

        // Refresh the widget display
        val appWidgetManager = AppWidgetManager.getInstance(this)
        updateWidget(this, appWidgetManager, widgetId)

        Toast.makeText(this, "✅ Note saved!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
