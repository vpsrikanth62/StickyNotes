package com.example.stickynote

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val a = intent?.action ?: return
        if (a == Intent.ACTION_BOOT_COMPLETED ||
            a == "android.intent.action.MY_PACKAGE_REPLACED") {
            MidnightPurgeScheduler.schedule(context)
            NoonPresetScheduler.schedule(context)
        }
    }
}
