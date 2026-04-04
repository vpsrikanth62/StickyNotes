package com.example.stickynote

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class NoonPresetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, @Suppress("UNUSED_PARAMETER") intent: Intent?) {
        val appCtx = context.applicationContext
        val presets = PresetTasksManager.getEnabledPresets(appCtx)
        if (presets.isNotEmpty()) {
            val mgr = AppWidgetManager.getInstance(appCtx)
            val ids = mgr.getAppWidgetIds(ComponentName(appCtx, StickyNoteWidget::class.java))
            for (id in ids) {
                val existing = NoteRepository.load(appCtx, id).toMutableList()
                val existingTexts = existing.map { it.text.lowercase().trim() }.toSet()
                val newItems = presets
                    .filter { it.lowercase().trim() !in existingTexts }
                    .map { NoteRepository.newItem(it) }
                if (newItems.isNotEmpty()) {
                    val unchecked = existing.filterNot { it.isChecked }
                    val checked = existing.filter { it.isChecked }
                    NoteRepository.save(appCtx, id, unchecked + newItems + checked)
                    mgr.notifyAppWidgetViewDataChanged(id, R.id.lv_notes)
                    updateWidget(appCtx, mgr, id)
                }
            }
        }
        NoonPresetScheduler.schedule(appCtx)
    }
}
