package com.example.stickynote

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews

const val PREFS_NAME = "StickyNotePrefs"
private const val ACTION_WALLPAPER_CHANGED = "android.intent.action.WALLPAPER_CHANGED"
private const val ACTION_FORCE_REFRESH = "com.example.stickynote.FORCE_WIDGET_REFRESH"
private const val TAG = "StickyNoteWidgetDbg"
const val EXTRA_AUTO_FOCUS_EMPTY = "extra_auto_focus_empty"
private val WIDGET_ROW_IDS = intArrayOf(
    R.id.tv_row_1,
    R.id.tv_row_2,
    R.id.tv_row_3,
    R.id.tv_row_4
)

class StickyNoteWidget : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d(TAG, "onReceive action=${intent.action}")
        if (intent.action == ACTION_WALLPAPER_CHANGED || intent.action == ACTION_FORCE_REFRESH) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, StickyNoteWidget::class.java))
            Log.d(TAG, "refresh for ids=${ids.joinToString()}")
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
    Log.d(TAG, "updateWidget start id=$widgetId")
    if (!NoteRepository.hasStoredItems(context, widgetId)) {
        val defaults = listOf(
            NoteRepository.newItem(context.getString(R.string.widget_default_item_1)),
            NoteRepository.newItem(context.getString(R.string.widget_default_item_2)),
            NoteRepository.newItem(context.getString(R.string.widget_default_item_3))
        )
        NoteRepository.save(context, widgetId, defaults)
    }
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

    views.setViewVisibility(R.id.lv_notes, View.GONE)
    views.setViewVisibility(R.id.compat_rows, if (total > 0) View.VISIBLE else View.GONE)
    views.setViewVisibility(R.id.tv_empty, if (total > 0) View.GONE else View.VISIBLE)
    bindWidgetRows(context, views, widgetId, items, style)

    // ── Open editor intents (edit button + empty/background tap) ───────
    val editIntent = Intent(context, EditNoteActivity::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        putExtra(EXTRA_AUTO_FOCUS_EMPTY, total == 0)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val editPI = PendingIntent.getActivity(
        context, widgetId + 1000, editIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.btn_widget_edit, editPI)
    views.setOnClickPendingIntent(R.id.widget_root, editPI)
    views.setOnClickPendingIntent(R.id.list_container, editPI)
    views.setOnClickPendingIntent(R.id.compat_rows, editPI)
    views.setOnClickPendingIntent(R.id.tv_empty, editPI)

    appWidgetManager.updateAppWidget(widgetId, views)
    Log.d(TAG, "updateWidget applied id=$widgetId total=$total")
}

private fun bindWidgetRows(
    context: Context,
    views: RemoteViews,
    widgetId: Int,
    items: List<NoteItem>,
    style: WidgetAppearance
) {
    val visibleCount = items.size.coerceAtMost(WIDGET_ROW_IDS.size)
    WIDGET_ROW_IDS.forEachIndexed { idx, viewId ->
        val item = items.getOrNull(idx)
        if (item == null) {
            views.setViewVisibility(viewId, View.GONE)
            return@forEachIndexed
        }

        val rowTextSp = computeRowTextSize(style.rowTextSp, visibleCount, item.text.length)
        val rowMaxLines = computeRowMaxLines(visibleCount, item.text.length)
        views.setViewVisibility(viewId, View.VISIBLE)
        views.setTextViewText(viewId, item.text)
        views.setTextColor(
            viewId,
            if (item.isChecked) style.rowTextDoneColor else style.rowTextActiveColor
        )
        val flags = if (item.isChecked) {
            Paint.ANTI_ALIAS_FLAG or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            Paint.ANTI_ALIAS_FLAG
        }
        views.setInt(viewId, "setPaintFlags", flags)
        views.setTextViewTextSize(viewId, TypedValue.COMPLEX_UNIT_SP, rowTextSp)
        views.setInt(viewId, "setBackgroundResource", style.rowBgRes)
        views.setBoolean(viewId, "setSingleLine", false)
        views.setInt(viewId, "setMinLines", 1)
        views.setInt(viewId, "setMaxLines", rowMaxLines)

        val rowIntent = Intent(context, WidgetActionReceiver::class.java).apply {
            action = ACTION_TOGGLE
            putExtra(EXTRA_WIDGET_ID, widgetId)
            putExtra(EXTRA_ITEM_ID, item.id)
            data = Uri.parse("stickynote://toggle/$widgetId/${item.id}")
        }
        val rowPI = PendingIntent.getBroadcast(
            context,
            widgetId * 100 + idx,
            rowIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setOnClickPendingIntent(viewId, rowPI)
    }
}

private fun computeRowTextSize(baseSp: Float, visibleCount: Int, textLen: Int): Float {
    return when (visibleCount) {
        1 -> when {
            textLen > 220 -> 12.0f
            textLen > 170 -> 13.0f
            textLen > 120 -> 14.2f
            textLen > 70 -> 15.8f
            else -> 18.8f
        }
        2 -> when {
            textLen > 150 -> 12.2f
            textLen > 100 -> 13.4f
            textLen > 65 -> 14.6f
            else -> 16.8f
        }
        3 -> when {
            textLen > 120 -> 11.8f
            textLen > 80 -> 12.8f
            else -> 14.2f
        }
        else -> baseSp
    }
}

private fun computeRowMaxLines(visibleCount: Int, textLen: Int): Int {
    return when {
        visibleCount <= 1 && textLen > 160 -> 24
        visibleCount <= 1 -> 18
        visibleCount == 2 && textLen > 110 -> 12
        visibleCount == 2 -> 9
        visibleCount == 3 -> 7
        else -> 5
    }
}
