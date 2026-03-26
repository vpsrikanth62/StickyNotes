package com.example.stickynote

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews

const val PREFS_NAME = "StickyNotePrefs"
private const val ACTION_WALLPAPER_CHANGED = "android.intent.action.WALLPAPER_CHANGED"

class StickyNoteWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WALLPAPER_CHANGED) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, StickyNoteWidget::class.java))
            ids.forEach { updateWidget(context, mgr, it) }
        }
    }

    override fun onEnabled(context: Context) {
        MidnightPurgeScheduler.schedule(context)
    }

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
    val style    = WidgetAppearanceResolver.resolve(context, total)
    val progress = if (total > 0) {
        context.getString(R.string.widget_progress_format, done, total)
    } else {
        context.getString(R.string.widget_progress_empty)
    }

    val views = RemoteViews(context.packageName, R.layout.widget_sticky_note)

    // Progress label
    views.setTextViewText(R.id.tv_progress, progress)
    views.setTextColor(R.id.tv_title, style.titleColor)
    views.setTextColor(R.id.tv_progress, style.subColor)
    views.setTextColor(R.id.widget_footer_hint, style.accentColor)
    views.setTextColor(R.id.tv_empty_title, style.emptyTitleColor)
    views.setTextColor(R.id.tv_empty_sub, style.emptySubColor)

    // Contrast-aware palette based on current wallpaper.
    views.setInt(R.id.widget_root, "setBackgroundResource", style.rootBgRes)
    views.setInt(R.id.btn_widget_edit, "setBackgroundResource", style.editBtnBgRes)
    views.setImageViewResource(R.id.btn_widget_edit, style.editIconRes)
    views.setInt(R.id.btn_widget_edit, "setColorFilter", style.accentColor)
    views.setInt(R.id.divider_header, "setBackgroundResource", style.dividerRes)

    // Helper hint removed by request.
    views.setViewVisibility(R.id.widget_footer_hint, View.GONE)

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
