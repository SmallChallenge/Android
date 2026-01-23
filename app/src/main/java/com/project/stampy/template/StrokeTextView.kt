package com.project.stampy.template

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet

// 템플릿 텍스트 외곽선 효과 커스텀
class StrokeTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {

    private var strokeWidth = 0f
    private var strokeColor = 0xFF000000.toInt()

    fun setStroke(width: Float, color: Int) {
        strokeWidth = width
        strokeColor = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (strokeWidth > 0) {
            // 외곽선 그리기
            val currentTextColor = currentTextColor
            paint.style = android.graphics.Paint.Style.STROKE
            paint.strokeWidth = strokeWidth
            paint.strokeJoin = android.graphics.Paint.Join.ROUND
            setTextColor(strokeColor)
            super.onDraw(canvas)

            // 채우기 그리기
            paint.style = android.graphics.Paint.Style.FILL
            setTextColor(currentTextColor)
        }
        super.onDraw(canvas)
    }
}