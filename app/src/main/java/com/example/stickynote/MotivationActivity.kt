package com.example.stickynote

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

val MOTIVATION_MESSAGES = listOf(
    "\uD83D\uDD25 Crushed it! One step closer to greatness.",
    "\uD83C\uDF1F You\'re on fire! Keep that energy going!",
    "\u2728 Done and dusted. You\'re unstoppable!",
    "\uD83D\uDE80 That\'s the spirit! Nothing can stop you now.",
    "\uD83C\uDFC6 Champion move! Small wins = big dreams.",
    "\uD83D\uDCAA Boom! Another one bites the dust.",
    "\uD83C\uDF88 Look at you go! Absolutely killing it.",
    "\u26A1 Lightning fast! You make it look easy.",
    "\uD83E\uDD81 Beast mode: ACTIVATED. Keep going!",
    "\uD83D\uDCA5 Tasks fear you. Legends respect you."
)

/** Fullscreen translucent glass overlay that shows a motivation message then auto-dismisses. */
class MotivationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyGlassWindow()
        setContentView(R.layout.activity_motivation)

        val msg = MOTIVATION_MESSAGES.random()
        findViewById<TextView>(R.id.tv_motivation).text = msg

        // Dismiss on tap anywhere
        findViewById<android.view.View>(R.id.motivation_root)
            .setOnClickListener { finish() }

        // Auto-dismiss after 2.8 seconds
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 2800)
    }

    private fun applyGlassWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            val attrs = window.attributes
            attrs.blurBehindRadius = 60
            window.attributes = attrs
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }
}
