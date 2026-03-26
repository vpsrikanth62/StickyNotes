package com.example.stickynote

import android.app.Application

class StickyNoteApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MidnightPurgeScheduler.schedule(this)
    }
}
