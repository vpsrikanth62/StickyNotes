package com.example.stickynote

import android.content.Context
import android.graphics.Color

data class WidgetAppearance(
    val useLightSurface: Boolean,
    val accentColor: Int,
    val rowTextSp: Float,
    val rowMinLines: Int,
    val rowMaxLines: Int,
    val rootBgRes: Int,
    val rowBgRes: Int,
    val editBtnBgRes: Int,
    val editIconRes: Int,
    val emptyCheckIconRes: Int,
    val dividerRes: Int,
    val titleColor: Int,
    val subColor: Int,
    val footerColor: Int,
    val emptyTitleColor: Int,
    val emptySubColor: Int,
    val rowTextActiveColor: Int,
    val rowTextDoneColor: Int
)

object WidgetAppearanceResolver {

    fun resolve(context: Context, itemCount: Int): WidgetAppearance {
        val accent = UiStylePrefs.accentColor(UiStylePrefs.accentMode(context))
        val rowTextSp = when {
            itemCount <= 1 -> 16.0f
            itemCount == 2 -> 15.0f
            itemCount == 3 -> 14.0f
            itemCount <= 6 -> 13.2f
            else -> 12.8f
        }

        return WidgetAppearance(
            useLightSurface = true,
            accentColor = accent,
            rowTextSp = rowTextSp,
            rowMinLines = 1,
            rowMaxLines = 12,
            rootBgRes = R.drawable.bg_widget_glass,
            rowBgRes = R.drawable.bg_widget_row_chip,
            editBtnBgRes = R.drawable.bg_widget_edit_btn,
            editIconRes = R.drawable.ic_widget_edit_dark,
            emptyCheckIconRes = R.drawable.ic_check_empty_dark,
            dividerRes = R.drawable.bg_divider_glass,
            titleColor = Color.parseColor("#E61A1A1A"),
            subColor = Color.parseColor("#99000000"),
            footerColor = Color.parseColor("#66000000"),
            emptyTitleColor = Color.parseColor("#99000000"),
            emptySubColor = Color.parseColor("#55000000"),
            rowTextActiveColor = Color.parseColor("#E61A1A1A"),
            rowTextDoneColor = Color.parseColor("#66000000")
        )
    }
}
