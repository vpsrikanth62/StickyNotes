package com.example.stickynote

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

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

/** Computes row text size and light/dark widget palette for legibility. */
object WidgetAppearanceResolver {

    fun resolve(context: Context, itemCount: Int): WidgetAppearance {
        val wallpaperLuma = estimateWallpaperLuma(context)
        // Dark wallpaper -> light surface; bright wallpaper -> dark surface.
        val useLightSurface = wallpaperLuma < 0.45f
        val accent = UiStylePrefs.accentColor(UiStylePrefs.accentMode(context))
        val rowTextSp = when {
            itemCount <= 1 -> 16.0f
            itemCount == 2 -> 15.0f
            itemCount == 3 -> 14.0f
            itemCount <= 6 -> 13.2f
            else -> 12.8f
        }
        val multiline = true

        return if (useLightSurface) {
            WidgetAppearance(
                useLightSurface = true,
                accentColor = accent,
                rowTextSp = rowTextSp,
                rowMinLines = if (multiline) 1 else 1,
                rowMaxLines = if (multiline) 12 else 1,
                rootBgRes = R.drawable.bg_widget_glass_light,
                rowBgRes = R.drawable.bg_widget_row_chip_light,
                editBtnBgRes = R.drawable.bg_widget_edit_btn_light,
                editIconRes = R.drawable.ic_widget_edit_dark,
                emptyCheckIconRes = R.drawable.ic_check_empty_dark,
                dividerRes = R.drawable.bg_divider_glass_dark,
                titleColor = Color.parseColor("#E61A1A1A"),
                subColor = Color.parseColor("#B3202020"),
                footerColor = Color.parseColor("#8A202020"),
                emptyTitleColor = Color.parseColor("#D9202020"),
                emptySubColor = Color.parseColor("#99202020"),
                // Light surface uses light row chips -> text must be dark for contrast.
                rowTextActiveColor = Color.parseColor("#E61A1A1A"),
                rowTextDoneColor = Color.parseColor("#8A202020")
            )
        } else {
            WidgetAppearance(
                useLightSurface = false,
                accentColor = accent,
                rowTextSp = rowTextSp,
                rowMinLines = if (multiline) 1 else 1,
                rowMaxLines = if (multiline) 12 else 1,
                rootBgRes = R.drawable.bg_widget_glass,
                rowBgRes = R.drawable.bg_widget_row_chip,
                editBtnBgRes = R.drawable.bg_widget_edit_btn,
                editIconRes = R.drawable.ic_widget_edit,
                emptyCheckIconRes = R.drawable.ic_check_empty,
                dividerRes = R.drawable.bg_divider_glass,
                titleColor = Color.parseColor("#FFFFFFFF"),
                subColor = Color.parseColor("#8AFFFFFF"),
                footerColor = Color.parseColor("#3AFFFFFF"),
                emptyTitleColor = Color.parseColor("#99FFFFFF"),
                emptySubColor = Color.parseColor("#55FFFFFF"),
                rowTextActiveColor = Color.parseColor("#F2FFFFFF"),
                rowTextDoneColor = Color.parseColor("#B3FFFFFF")
            )
        }
    }

    private fun estimateWallpaperLuma(context: Context): Float {
        return runCatching {
            val drawable = WallpaperManager.getInstance(context).drawable ?: return 0.5f
            if (drawable is ColorDrawable) {
                return colorLuma(drawable.color)
            }

            val bmp = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            val copy = drawable.constantState?.newDrawable()?.mutate() ?: drawable
            copy.setBounds(0, 0, canvas.width, canvas.height)
            copy.draw(canvas)

            var lumaSum = 0f
            var count = 0
            for (y in 0 until bmp.height step 2) {
                for (x in 0 until bmp.width step 2) {
                    lumaSum += colorLuma(bmp.getPixel(x, y))
                    count++
                }
            }
            bmp.recycle()
            if (count == 0) 0.5f else lumaSum / count
        }.getOrDefault(0.5f)
    }

    private fun colorLuma(color: Int): Float {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return (0.299f * r + 0.587f * g + 0.114f * b) / 255f
    }
}

