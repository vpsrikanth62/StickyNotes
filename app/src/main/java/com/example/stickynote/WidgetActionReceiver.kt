package com.example.stickynote

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

const val EXTRA_WIDGET_ID = "extra_widget_id"
private const val DOUBLE_TAP_PREFS = "WidgetTapPrefs"
private const val KEY_LAST_WIDGET = "last_widget"
private const val KEY_LAST_ITEM = "last_item"
private const val KEY_LAST_MS = "last_ms"
private const val DOUBLE_TAP_WINDOW_MS = 550L

/**
 * Receives ACTION_TOGGLE broadcasts from widget row taps.
 * Toggles the item and refreshes the widget.
 */
class WidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TOGGLE) return

        val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val itemId   = intent.getStringExtra(EXTRA_ITEM_ID) ?: return

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        // Require double-tap on same row to avoid accidental toggles.
        val now = System.currentTimeMillis()
        val tapPrefs = context.getSharedPreferences(DOUBLE_TAP_PREFS, Context.MODE_PRIVATE)
        val lastWidget = tapPrefs.getInt(KEY_LAST_WIDGET, AppWidgetManager.INVALID_APPWIDGET_ID)
        val lastItem = tapPrefs.getString(KEY_LAST_ITEM, null)
        val lastMs = tapPrefs.getLong(KEY_LAST_MS, 0L)
        val isConfirmedDoubleTap =
            widgetId == lastWidget &&
            itemId == lastItem &&
            now - lastMs in 1..DOUBLE_TAP_WINDOW_MS

        if (!isConfirmedDoubleTap) {
            tapPrefs.edit()
                .putInt(KEY_LAST_WIDGET, widgetId)
                .putString(KEY_LAST_ITEM, itemId)
                .putLong(KEY_LAST_MS, now)
                .apply()
            return
        }

        tapPrefs.edit()
            .remove(KEY_LAST_WIDGET)
            .remove(KEY_LAST_ITEM)
            .remove(KEY_LAST_MS)
            .apply()

        NoteRepository.toggle(context, widgetId, itemId)

        val manager = AppWidgetManager.getInstance(context)
        manager.notifyAppWidgetViewDataChanged(widgetId, R.id.lv_notes)
        updateWidget(context, manager, widgetId)
    }
}
