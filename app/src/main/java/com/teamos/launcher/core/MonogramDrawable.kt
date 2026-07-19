package com.teamos.launcher.core

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import kotlin.math.min

/**
 * Fallback app icon: a brand-colored rounded tile with the app's initial.
 * Used while a real logo loads or when none can be fetched. Never an emoji.
 */
class MonogramDrawable(name: String, seed: String) : Drawable() {

    private val letter = (name.trim().firstOrNull()?.uppercaseChar() ?: '?').toString()
    private val bg = colorFor(seed)

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bg }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    override fun draw(canvas: Canvas) {
        val b = bounds
        val r = min(b.width(), b.height()) * 0.28f
        bgPaint.color = bg
        canvas.drawRoundRect(RectF(b), r, r, bgPaint)
        textPaint.textSize = min(b.width(), b.height()) * 0.5f
        val cx = b.exactCenterX()
        val cy = b.exactCenterY() - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(letter, cx, cy, textPaint)
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(cf: android.graphics.ColorFilter?) {}
    override fun getOpacity(): Int = PixelFormat.OPAQUE

    companion object {
        private val palette = intArrayOf(
            0xFF4F6BFF.toInt(), 0xFF7A5BFF.toInt(), 0xFF00A67E.toInt(),
            0xFFEA4335.toInt(), 0xFFFBBC05.toInt(), 0xFF34A853.toInt(),
            0xFFFF6D00.toInt(), 0xFF0088CC.toInt(), 0xFFE91E63.toInt(),
            0xFF00BCD4.toInt(), 0xFF8E44AD.toInt(), 0xFF16A085.toInt()
        )

        private fun colorFor(seed: String): Int {
            var h = 0
            for (c in seed) h = h * 31 + c.code
            return palette[((h % palette.size) + palette.size) % palette.size]
        }
    }
}
