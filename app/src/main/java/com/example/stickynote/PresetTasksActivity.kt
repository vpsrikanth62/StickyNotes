package com.example.stickynote

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PresetTasksActivity : AppCompatActivity() {

    private lateinit var rvPresets: RecyclerView
    private lateinit var etNewPreset: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnClose: Button
    private lateinit var btnSave: Button
    private lateinit var adapter: PresetTaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preset_tasks)

        applyWindowFlags()

        rvPresets = findViewById(R.id.rv_presets)
        etNewPreset = findViewById(R.id.et_new_preset)
        btnAdd = findViewById(R.id.btn_add_preset)
        btnClose = findViewById(R.id.btn_close)
        btnSave = findViewById(R.id.btn_save_presets)

        val presets = PresetTasksManager.loadPresets(this).toMutableList()
        adapter = PresetTaskAdapter(presets)
        rvPresets.layoutManager = LinearLayoutManager(this)
        rvPresets.adapter = adapter

        btnAdd.setOnClickListener {
            val text = etNewPreset.text?.toString()?.trim() ?: ""
            if (text.isNotEmpty()) {
                adapter.addItem(text)
                etNewPreset.text?.clear()
                rvPresets.smoothScrollToPosition(adapter.itemCount - 1)
            }
        }

        btnClose.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            PresetTasksManager.savePresets(this, adapter.getItems())
            NoonPresetScheduler.schedule(this)
            Toast.makeText(this, "Presets saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun applyWindowFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val lp = window.attributes
            lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            lp.blurBehindRadius = 40
            lp.dimAmount = 0.10f
            window.attributes = lp
            window.setBackgroundBlurRadius(48)
        }
    }
}
