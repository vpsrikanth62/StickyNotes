package com.example.stickynote

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/** Schedules a one-shot alarm for the next local midnight; [MidnightPurgeReceiver] reschedules after running. */
object MidnightPurgeScheduler {

    private const val REQUEST_ID = 0x4D4944 // "MID"

    fun schedule(context: Context) {
        val appCtx = context.applicationContext
        val am = appCtx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(appCtx, MidnightPurgeReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            appCtx,
            REQUEST_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = nextLocalMidnightMillis()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // No SCHEDULE_EXACT_ALARM needed; may be deferred slightly in Doze
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            @Suppress("DEPRECATION")
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    /** Next 00:00:00.000 local time; if already past today, tomorrow. */
    fun nextLocalMidnightMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
