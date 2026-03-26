package com.example.stickynote

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

const val PREFS_NAME = "StickyNotePrefs"
const val PREF_KEY_PREFIX = "note_widget_"

class StickyNoteWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // Clean up saved notes when widget is removed
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        appWidgetIds.forEach { editor.remove("$PREF_KEY_PREFIX$it") }
        editor.apply()
    }
}

fun updateWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetId: Int
) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val noteText = prefs.getString("$PREF_KEY_PREFIX$widgetId", "") ?: ""

    val views = RemoteViews(context.packageName, R.layout.widget_sticky_note)

    if (noteText.isEmpty()) {
        views.setTextViewText(R.id.tv_note, context.getString(R.string.placeholder_text))
    } else {
        views.setTextViewText(R.id.tv_note, noteText)
    }

    // Tap on widget opens the EditNoteActivity
    val editIntent = Intent(context, EditNoteActivity::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        widgetId,
        editIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

    appWidgetManager.updateAppWidget(widgetId, views)
}
