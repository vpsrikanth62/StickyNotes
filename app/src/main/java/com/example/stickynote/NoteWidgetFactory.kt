package com.example.stickynote

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import android.widget.RemoteViewsService

const val ACTION_TOGGLE = "com.example.stickynote.TOGGLE_ITEM"
const val EXTRA_ITEM_ID = "extra_item_id"

/**
 * Provides one RemoteViews row per NoteItem for the widget ListView.
 * Each row has a checkbox icon + text. Tapping a row broadcasts ACTION_TOGGLE.
 */
class NoteWidgetFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private val widgetId = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )
    private var items: List<NoteItem> = emptyList()
    private var style: WidgetAppearance = WidgetAppearanceResolver.resolve(context, 0)

    override fun onCreate() { reload() }
    override fun onDataSetChanged() { reload() }
    override fun onDestroy() {}

    private fun reload() {
        items = NoteRepository.load(context, widgetId)
        style = WidgetAppearanceResolver.resolve(context, items.size)
    }

    override fun getCount() = items.size
    override fun getItemId(position: Int) = position.toLong()
    override fun hasStableIds() = true
    override fun getLoadingView() = null
    override fun getViewTypeCount() = 1

    override fun getViewAt(position: Int): RemoteViews {
        val item = items.getOrNull(position)
            ?: return RemoteViews(context.packageName, R.layout.widget_note_row)

        return RemoteViews(context.packageName, R.layout.widget_note_row).apply {
            setInt(R.id.row_root, "setBackgroundResource", style.rowBgRes)
            setTextViewTextSize(R.id.tv_item_text, TypedValue.COMPLEX_UNIT_SP, style.rowTextSp)
            setInt(R.id.tv_item_text, "setMinLines", style.rowMinLines)
            setInt(R.id.tv_item_text, "setMaxLines", style.rowMaxLines)
            // No ellipsis: allow long tasks to wrap into multiple lines.
            setBoolean(R.id.tv_item_text, "setSingleLine", false)
            val density = context.resources.displayMetrics.density
            val minRowHeightPx = (34 * density).toInt()
            setInt(R.id.row_root, "setMinimumHeight", minRowHeightPx)

            // Text: strikethrough + dimmed if done, bright if active
            if (item.isChecked) {
                setTextViewText(R.id.tv_item_text, item.text)
                setInt(R.id.tv_item_text, "setPaintFlags",
                    Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
                setTextColor(R.id.tv_item_text, style.rowTextDoneColor)
            } else {
                setTextViewText(R.id.tv_item_text, item.text)
                setInt(R.id.tv_item_text, "setPaintFlags", Paint.ANTI_ALIAS_FLAG)
                setTextColor(R.id.tv_item_text, style.rowTextActiveColor)
            }

            // Fill-in intent carries the item ID for the toggle action
            val fillIn = Intent().apply {
                val bundle = Bundle()
                bundle.putString(EXTRA_ITEM_ID, item.id)
                putExtras(bundle)
            }
            setOnClickFillInIntent(R.id.row_root, fillIn)
        }
    }
}
