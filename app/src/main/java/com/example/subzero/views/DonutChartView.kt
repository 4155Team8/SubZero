package com.example.subzero.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

data class DonutSlice(val value: Float, val color: Int)

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }

    private val oval = RectF()
    var slices: List<DonutSlice> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val strokeWidth = context.resources.displayMetrics.density * 48f
    private val gapDegrees = 3f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (slices.isEmpty()) return

        val total = slices.sumOf { it.value.toDouble() }.toFloat()
        if (total == 0f) return

        paint.strokeWidth = strokeWidth
        val inset = strokeWidth / 2f + 8f
        oval.set(inset, inset, width - inset, height - inset)

        val totalGap = gapDegrees * slices.size
        val availableDegrees = 360f - totalGap

        var startAngle = -90f
        slices.forEach { slice ->
            val sweep = (slice.value / total) * availableDegrees
            paint.color = slice.color
            canvas.drawArc(oval, startAngle, sweep, false, paint)
            startAngle += sweep + gapDegrees
        }
    }
}
