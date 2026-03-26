package com.example.stickynote

import android.content.Intent
import android.widget.RemoteViewsService

/** Binds the ListView inside the widget to NoteWidgetFactory. */
class NoteWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        NoteWidgetFactory(applicationContext, intent)
}
