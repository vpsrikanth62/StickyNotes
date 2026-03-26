package com.example.stickynote

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
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

    override fun onCreate() { reload() }
    override fun onDataSetChanged() { reload() }
    override fun onDestroy() {}

    private fun reload() {
        items = NoteRepository.load(context, widgetId)
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
            // Checkbox icon: filled check vs empty circle
            setImageViewResource(
                R.id.iv_checkbox,
                if (item.isChecked) R.drawable.ic_check_done else R.drawable.ic_check_empty
            )

            // Text: strikethrough + dimmed if done, bright if active
            if (item.isChecked) {
                setTextViewText(R.id.tv_item_text, item.text)
                setInt(R.id.tv_item_text, "setPaintFlags",
                    Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
                setTextColor(R.id.tv_item_text, 0x55FFFFFF.toInt())
            } else {
                setTextViewText(R.id.tv_item_text, item.text)
                setInt(R.id.tv_item_text, "setPaintFlags", Paint.ANTI_ALIAS_FLAG)
                setTextColor(R.id.tv_item_text, 0xF0FFFFFF.toInt())
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
