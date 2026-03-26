package com.example.stickynote

import android.content.Context
import android.graphics.Color

object UiStylePrefs {
    const val PREFS = "EditorUiPrefs"
    const val KEY_FONT_MODE = "font_mode"
    const val KEY_ACCENT_MODE = "accent_mode"

    const val FONT_MONTSERRAT = "montserrat"
    const val FONT_SYSTEM = "system"
    const val FONT_SERIF = "serif"
    const val FONT_MONO = "mono"
    const val FONT_MEDIUM = "medium"
    const val FONT_CONDENSED = "condensed"

    const val ACCENT_VIOLET = "violet"
    const val ACCENT_CYAN = "cyan"
    const val ACCENT_ROSE = "rose"
    const val ACCENT_EMERALD = "emerald"
    const val ACCENT_AMBER = "amber"
    const val ACCENT_INDIGO = "indigo"
    const val ACCENT_CRIMSON = "crimson"
    const val ACCENT_TEAL = "teal"

    fun fontMode(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_FONT_MODE, FONT_MONTSERRAT) ?: FONT_MONTSERRAT

    fun accentMode(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ACCENT_MODE, ACCENT_VIOLET) ?: ACCENT_VIOLET

    fun accentColor(mode: String): Int = when (mode) {
        ACCENT_CYAN -> Color.parseColor("#06B6D4")
        ACCENT_ROSE -> Color.parseColor("#EC4899")
        ACCENT_EMERALD -> Color.parseColor("#10B981")
        ACCENT_AMBER -> Color.parseColor("#F59E0B")
        ACCENT_INDIGO -> Color.parseColor("#6366F1")
        ACCENT_CRIMSON -> Color.parseColor("#DC2626")
        ACCENT_TEAL -> Color.parseColor("#14B8A6")
        else -> Color.parseColor("#7C3AED")
    }

    fun accentGradient(mode: String): Pair<Int, Int> = when (mode) {
        ACCENT_CYAN -> Color.parseColor("#06B6D4") to Color.parseColor("#6366F1")
        ACCENT_ROSE -> Color.parseColor("#EC4899") to Color.parseColor("#FF2D78")
        ACCENT_EMERALD -> Color.parseColor("#10B981") to Color.parseColor("#14B8A6")
        ACCENT_AMBER -> Color.parseColor("#F59E0B") to Color.parseColor("#FB7185")
        ACCENT_INDIGO -> Color.parseColor("#6366F1") to Color.parseColor("#A855F7")
        ACCENT_CRIMSON -> Color.parseColor("#DC2626") to Color.parseColor("#F43F5E")
        ACCENT_TEAL -> Color.parseColor("#14B8A6") to Color.parseColor("#22D3EE")
        else -> Color.parseColor("#7C3AED") to Color.parseColor("#EC4899")
    }
}

