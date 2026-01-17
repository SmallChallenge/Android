package com.project.stampy.template

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.project.stampy.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * 템플릿 바인더 베이스 클래스
 * 공통 기능 제공
 */
abstract class TemplateBinderBase(
    protected val context: Context,
    protected val root: View
) {
    /**
     * 템플릿 바인딩 (각 카테고리별 구현)
     */
    abstract fun bind(template: Template, showLogo: Boolean, photoTakenAtTimestamp: Long)

    /**
     * 폰트 로드 (안전)
     */
    protected fun loadFont(fontResId: Int): android.graphics.Typeface? {
        return try {
            ResourcesCompat.getFont(context, fontResId)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 텍스트 그림자 적용 (공통)
     * x=0, y=0, 흐림=5px, #000000 45%
     */
    protected fun applyTextShadow(textView: TextView) {
        textView.setShadowLayer(
            DesignUtils.dpToPx(context, 5f),  // 블러 반경 5px
            0f,  // x 오프셋 0
            0f,  // y 오프셋 0
            0x73000000.toInt()  // #000000 45%
        )
    }

    /**
     * 텍스트 그림자 적용 (커스텀)
     */
    protected fun applyTextShadow(
        textView: TextView,
        blurRadius: Float,
        dx: Float,
        dy: Float,
        color: Int
    ) {
        textView.setShadowLayer(
            DesignUtils.dpToPx(context, blurRadius),
            DesignUtils.dpToPx(context, dx),
            DesignUtils.dpToPx(context, dy),
            color
        )
    }

    /**
     * 로고 표시/숨김
     */
    protected fun setLogoVisibility(logoId: Int, showLogo: Boolean) {
        root.findViewById<ImageView>(logoId)?.visibility =
            if (showLogo) View.VISIBLE else View.GONE
    }

    /**
     * TextView 설정 (폰트, 크기, 그림자)
     */
    protected fun setupTextView(
        textView: TextView?,
        text: String,
        font: android.graphics.Typeface?,
        textSize: Float,
        applyShadow: Boolean = true
    ) {
        textView?.apply {
            this.text = text
            font?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, textSize)
            if (applyShadow) {
                applyTextShadow(this)
            }
        }
    }

    /**
     * 마진 설정
     */
    protected fun setMargin(
        view: View?,
        top: Int = -1,
        bottom: Int = -1,
        start: Int = -1,
        end: Int = -1
    ) {
        (view?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            if (top >= 0) topMargin = top
            if (bottom >= 0) bottomMargin = bottom
            if (start >= 0) marginStart = start
            if (end >= 0) marginEnd = end
            view.layoutParams = this
        }
    }

    /**
     * 날짜 포맷 (공통)
     */
    protected fun formatDate(pattern: String, timestamp: Long, locale: Locale = Locale.getDefault()): String {
        val format = SimpleDateFormat(pattern, locale)
        return format.format(Date(timestamp))
    }

    /**
     * 요일 한글 변환
     */
    protected fun getDayOfWeekKorean(calendar: Calendar): String {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "일"
            Calendar.MONDAY -> "월"
            Calendar.TUESDAY -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY -> "목"
            Calendar.FRIDAY -> "금"
            Calendar.SATURDAY -> "토"
            else -> ""
        }
    }

    /**
     * 요일 영문 약어
     */
    protected fun getDayOfWeekEnglish(calendar: Calendar): String {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> ""
        }
    }

    /**
     * 날짜 서수 접미사 (1st, 2nd, 3rd, 4th...)
     */
    protected fun getDaySuffix(day: Int): String {
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
    }
}