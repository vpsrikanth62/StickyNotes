package com.example.stickynote

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
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

        // Enable blur-behind on Android 12+ for true glass effect
        applyBlurBehind()

        setContentView(R.layout.activity_edit_note)

        // Show keyboard immediately
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        bindViews()
        loadSavedNote()
    }

    private fun applyBlurBehind() {
        // True frosted glass blur on API 31+ (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.attributes.blurBehindRadius = 48
        }
    }

    private fun bindViews() {
        etNote = findViewById(R.id.et_note)
        btnSave = findViewById(R.id.btn_save)
        btnCancel = findViewById(R.id.btn_cancel)
        tvCharCount = findViewById(R.id.tv_char_count)

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

    private fun loadSavedNote() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedNote = prefs.getString("$PREF_KEY_PREFIX$widgetId", "") ?: ""
        etNote.setText(savedNote)
        etNote.setSelection(savedNote.length)
        updateCharCount(savedNote.length)
    }

    private fun updateCharCount(length: Int) {
        tvCharCount.text = "$length"
    }

    private fun saveNote() {
        val noteText = etNote.text.toString()

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("$PREF_KEY_PREFIX$widgetId", noteText).apply()

        val appWidgetManager = AppWidgetManager.getInstance(this)
        updateWidget(this, appWidgetManager, widgetId)

        Toast.makeText(this, "Saved ✨", Toast.LENGTH_SHORT).show()
        finish()
    }
}
