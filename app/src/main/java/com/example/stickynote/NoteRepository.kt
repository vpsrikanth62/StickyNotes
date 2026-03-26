package com.example.stickynote

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import java.util.UUID

/** Single source of truth for all note items, keyed by widget ID. */
object NoteRepository {

    private const val KEY_PREFIX = "items_widget_"

    fun load(context: Context, widgetId: Int): List<NoteItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json  = prefs.getString("$KEY_PREFIX$widgetId", "") ?: ""
        return if (json.isEmpty()) emptyList() else json.toNoteItems()
    }

    fun save(context: Context, widgetId: Int, items: List<NoteItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("$KEY_PREFIX$widgetId", items.toJsonString()).apply()
    }

    fun toggle(context: Context, widgetId: Int, itemId: String): Boolean {
        val items = load(context, widgetId)
        val updated = items.map { if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it }
        // Keep active tasks first, completed tasks at the bottom.
        val sorted = updated.filterNot { it.isChecked } + updated.filter { it.isChecked }
        save(context, widgetId, sorted)
        // Return true if the toggled item is now checked (just completed)
        return sorted.find { it.id == itemId }?.isChecked == true
    }

    fun newItem(text: String = "") = NoteItem(
        id   = UUID.randomUUID().toString(),
        text = text
    )

    fun delete(context: Context, widgetId: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove("$KEY_PREFIX$widgetId").apply()
    }

    /** Drops checked items for every placed widget (runs at local midnight). */
    fun purgeCompletedItems(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        val ids = mgr.getAppWidgetIds(ComponentName(context, StickyNoteWidget::class.java))
        for (id in ids) {
            val items = load(context, id).filterNot { it.isChecked }
            save(context, id, items)
        }
    }
}
