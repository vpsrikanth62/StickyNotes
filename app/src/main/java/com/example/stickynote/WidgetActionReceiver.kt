package com.example.stickynote

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

const val EXTRA_WIDGET_ID = "extra_widget_id"

/**
 * Receives ACTION_TOGGLE broadcasts from widget row taps.
 * Toggles the item, refreshes the widget, and if item just got checked,
 * launches MotivationActivity with a glass overlay.
 */
class WidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TOGGLE) return

        val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val itemId   = intent.getStringExtra(EXTRA_ITEM_ID) ?: return

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        // Toggle and find out if this item just became DONE
        val justCompleted = NoteRepository.toggle(context, widgetId, itemId)

        // Refresh widget list
        val manager = AppWidgetManager.getInstance(context)
        manager.notifyAppWidgetViewDataChanged(widgetId, R.id.lv_notes)
        updateWidget(context, manager, widgetId)

        // Show motivation only when an item is freshly checked
        if (justCompleted) {
            val motIntent = Intent(context, MotivationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(motIntent)
        }
    }
}
