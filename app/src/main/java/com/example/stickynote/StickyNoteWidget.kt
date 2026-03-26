package com.example.stickynote

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

const val PREFS_NAME = "StickyNotePrefs"
const val PREF_KEY_PREFIX = "note_widget_"

class StickyNoteWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { updateWidget(context, appWidgetManager, it) }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { NoteRepository.delete(context, it) }
    }
}

fun updateWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetId: Int
) {
    val items    = NoteRepository.load(context, widgetId)
    val done     = items.count { it.isChecked }
    val total    = items.size
    val progress = if (total > 0) "$done / $total done" else "No notes yet"

    val views = RemoteViews(context.packageName, R.layout.widget_sticky_note)

    // Progress label
    views.setTextViewText(R.id.tv_progress, progress)

    // ── Wire ListView to NoteWidgetService ───────────────────────────────
    val serviceIntent = Intent(context, NoteWidgetService::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        // Unique data URI prevents Android from reusing the wrong factory
        data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
    }
    views.setRemoteAdapter(R.id.lv_notes, serviceIntent)
    views.setEmptyView(R.id.lv_notes, R.id.tv_empty)

    // ── Template PendingIntent for row taps (toggle checkbox) ────────────────
    val toggleIntent = Intent(context, WidgetActionReceiver::class.java).apply {
        action = ACTION_TOGGLE
        putExtra(EXTRA_WIDGET_ID, widgetId)
        data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
    }
    val togglePI = PendingIntent.getBroadcast(
        context, widgetId, toggleIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
    views.setPendingIntentTemplate(R.id.lv_notes, togglePI)

    // ── Edit button (pencil icon in header) ─────────────────────────────
    val editIntent = Intent(context, EditNoteActivity::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val editPI = PendingIntent.getActivity(
        context, widgetId + 1000, editIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.btn_widget_edit, editPI)

    appWidgetManager.updateAppWidget(widgetId, views)
}
