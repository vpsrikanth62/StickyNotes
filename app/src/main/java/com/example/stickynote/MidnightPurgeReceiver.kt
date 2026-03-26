package com.example.stickynote

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/** Removes completed items from every Sticky Note widget, then schedules the next midnight run. */
class MidnightPurgeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, @Suppress("UNUSED_PARAMETER") intent: Intent?) {
        val appCtx = context.applicationContext
        NoteRepository.purgeCompletedItems(appCtx)
        val mgr = AppWidgetManager.getInstance(appCtx)
        val ids = mgr.getAppWidgetIds(ComponentName(appCtx, StickyNoteWidget::class.java))
        for (id in ids) {
            mgr.notifyAppWidgetViewDataChanged(id, R.id.lv_notes)
            updateWidget(appCtx, mgr, id)
        }
        MidnightPurgeScheduler.schedule(appCtx)
    }
}
