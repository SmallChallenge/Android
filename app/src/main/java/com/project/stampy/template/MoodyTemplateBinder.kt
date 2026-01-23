package com.project.stampy.template

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.project.stampy.R
import java.util.*

/**
 * Moody 카테고리 템플릿 바인더
 */
class MoodyTemplateBinder(
    context: Context,
    root: View
) : TemplateBinderBase(context, root) {

    override fun bind(template: Template, showLogo: Boolean, photoTakenAtTimestamp: Long) {
        when (template.id) {
            "moody_1" -> bindMoody1(showLogo, photoTakenAtTimestamp)
            "moody_2" -> bindMoody2(showLogo, photoTakenAtTimestamp)
            "moody_3" -> bindMoody3(showLogo, photoTakenAtTimestamp)
            "moody_4" -> bindMoody4(showLogo, photoTakenAtTimestamp)
            "moody_5" -> bindMoody5(showLogo, photoTakenAtTimestamp)
            "moody_6" -> bindMoody6(showLogo, photoTakenAtTimestamp)
        }
    }

    /**
     * Moody 1 템플릿 데이터 바인딩
     */
    private fun bindMoody1(showLogo: Boolean, timestamp: Long) {
        val movesansFont = loadFont(R.font.movesans) // movesans 폰트 로드

        // 날짜 설정 (E, d MMM), 행간 100%
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        val currentDate = formatDate("E, d MMM", timestamp, Locale.US).uppercase(Locale.US)
        setupTextView(
            tvDate,
            currentDate,
            movesansFont,
            DesignUtils.getScaledTextSize(context, 30f),
            applyShadow = true,
            lineSpacingMultiplier = 1.0f
        )

        // 시간 설정 (a hh:mm, Locale: US), 행간 100%
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val currentTime = formatDate("a hh:mm", timestamp, Locale.US).uppercase(Locale.US)
        setupTextView(
            tvTime,
            currentTime,
            movesansFont,
            DesignUtils.getScaledTextSize(context, 16f),
            applyShadow = true,
            lineSpacingMultiplier = 1.0f
        )

        // 로고
        val ivLogo = root.findViewById<ImageView>(R.id.iv_stampic_logo)
        ivLogo?.visibility = if (showLogo) View.VISIBLE else View.GONE

        // 여백 조정
        setMargin(tvDate, top = DesignUtils.dpToPxInt(context, 24f))    // 날짜: 상단에서 24px
        setMargin(tvTime, top = DesignUtils.dpToPxInt(context, 4f))     // 시간: 날짜 밑으로 4px
        setMargin(
            ivLogo,
            bottom = DesignUtils.dpToPxInt(context, 11f) // 로고: 하단 16px (Blur 5 설정이 있어서 5dp 뺀 11f임)
        )
    }

    /**
     * Moody 2 템플릿 데이터 바인딩
     */
    private fun bindMoody2(showLogo: Boolean, timestamp: Long) {
        val suiteExtraLight = loadFont(R.font.suit_extralight)  // Suite ExtraLight 폰트 로드
        val suiteBold = loadFont(R.font.suit_bold)  // Suite Bold 폰트 로드

        val currentTime = formatDate("HH:mm", timestamp)
        val timeParts = currentTime.split(":")
        val hour = timeParts[0]
        val minute = timeParts[1]

        // 375px 기준 가변 크기 계산
        val scaledTimeSize = DesignUtils.getScaledTextSize(context, 100f)
        val scaledDateSize = DesignUtils.getScaledTextSize(context, 20f)

        // 그림자: x0 y0 blur10 #000000 30%
        val shadowRadius = DesignUtils.dpToPx(context, 10f)
        val shadowColor = 0x4D000000.toInt() // 30%

        // 시간 첫째 자리 (행간 100%)
        root.findViewById<TextView>(R.id.tv_hour_1)?.apply {
            text = hour[0].toString()
            suiteExtraLight?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledTimeSize)
            setLineSpacing(0f, 1.0f)  // 행간 100%
            setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
        }

        // 시간 둘째 자리 (행간 100%)
        root.findViewById<TextView>(R.id.tv_hour_2)?.apply {
            text = hour[1].toString()
            suiteExtraLight?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledTimeSize)
            setLineSpacing(0f, 1.0f)  // 행간 100%
            setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
        }

        // 분 첫째 자리 (행간 100%)
        root.findViewById<TextView>(R.id.tv_minute_1)?.apply {
            text = minute[0].toString()
            suiteExtraLight?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledTimeSize)
            setLineSpacing(0f, 1.0f)  // 행간 100%
            setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
        }

        // 분 둘째 자리 (행간 100%)
        root.findViewById<TextView>(R.id.tv_minute_2)?.apply {
            text = minute[1].toString()
            suiteExtraLight?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledTimeSize)
            setLineSpacing(0f, 1.0f)  // 행간 100%
            setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
        }

        // 날짜 (YYYY.MM.DD) (행간 Auto - 설정 안 함)
        root.findViewById<TextView>(R.id.tv_date)?.apply {
            text = formatDate("yyyy.MM.dd", timestamp)
            suiteBold?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledDateSize)
            applyTextShadow(this)   // 기본 그림자 적용
        }

        // 로고
        setLogoVisibility(R.id.iv_stampic_logo, showLogo)

        // 여백 조정
        setMargin(
            root.findViewById(R.id.iv_stampic_logo),
            top = DesignUtils.dpToPxInt(context, 16f) // 로고: 상단 여백 16px
        )

        // 원형 배경: 220dp 크기를 기기에 맞춰 조정
        val ivCircleBg = root.findViewById<ImageView>(R.id.iv_circle_bg)
        (ivCircleBg?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            // Blur 20은 사방으로 20px씩 번지기 때문에, 전체 이미지의 가로/세로 길이는 원래보다 40px 더 커짐
            val circleSize = DesignUtils.dpToPxInt(context, 260f)   // 260dp지만 blur 제외하면 실제 크기는 220dp 맞음
            width = circleSize
            height = circleSize
            ivCircleBg.layoutParams = this
        }

        setMargin(
            root.findViewById(R.id.tv_date),
            bottom = DesignUtils.dpToPxInt(context, 16f) // 날짜: 하단 여백 16px
        )

        // 숫자 간격 6px (Space로 이미 설정됨)
    }

    /**
     * Moody 3 템플릿 데이터 바인딩 (폴라로이드 스타일)
     */
    private fun bindMoody3(showLogo: Boolean, timestamp: Long) {
        val partialSansKrFont = loadFont(R.font.partial_sans_kr)    // Partial Sans KR 폰트 로드
        val suitHeavy = loadFont(R.font.suit_heavy)     // SUIT Heavy 폰트 로드
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 시간 설정 (am/pm HH:mm), 행간 Auto
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val amPm = if (hour < 12) "am" else "pm"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val timeText = String.format("%s %02d:%02d", amPm, displayHour, minute)

        setupTextView(
            tvTime,
            timeText,
            partialSansKrFont,
            DesignUtils.getScaledTextSize(context, 30f),
            applyShadow = false,
            lineSpacingMultiplier = null  // 행간 Auto
        )

        // 날짜 설정 (YYYY.MM.DD.요일), 행간 Auto
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // 요일 구하기 (영문 대문자)
        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "SUN"
            Calendar.MONDAY -> "MON"
            Calendar.TUESDAY -> "TUE"
            Calendar.WEDNESDAY -> "WED"
            Calendar.THURSDAY -> "THU"
            Calendar.FRIDAY -> "FRI"
            Calendar.SATURDAY -> "SAT"
            else -> ""
        }

        val dateText = String.format("%d.%02d.%02d.%s", year, month, day, dayOfWeek)

        setupTextView(
            tvDate,
            dateText,
            suitHeavy,
            DesignUtils.getScaledTextSize(context, 14f),
            applyShadow = false,
            lineSpacingMultiplier = null  // 행간 Auto
        )

        // 로고
        setLogoVisibility(R.id.iv_stampic_logo, showLogo)

        // 여백은 XML에서 다 처리되므로 별도 조정 불필요
    }

    /**
     * Moody 4 템플릿 데이터 바인딩
     */
    private fun bindMoody4(showLogo: Boolean, timestamp: Long) {
        val kkubulim = loadFont(R.font.bm_kkubulim) // 꾸불림체 폰트 로드
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 시간 설정 (오전/오후 h:mm)
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "오전" else "오후"
        val hour = calendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val minute = String.format("%02d", calendar.get(Calendar.MINUTE))

        setupTextView(
            tvTime,
            "$amPm $hour:$minute",
            kkubulim,
            DesignUtils.getScaledTextSize(context, 36f),
            applyShadow = false,
            lineSpacingMultiplier = null // 행간 Auto
        )
        applyTextShadow(tvTime)

        // 날짜 설정 (YYYY. MM. DD (요일))
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        setupTextView(
            tvDate,
            formatDate("yyyy. MM. dd (${getDayOfWeekKorean(calendar)})", timestamp),
            kkubulim,
            DesignUtils.getScaledTextSize(context, 18f),
            applyShadow = false,
            lineSpacingMultiplier = null // 행간 Auto
        )
        applyTextShadow(tvDate)

        // 하단 문구 설정 ("수고했어 오늘도")
        val tvMessage = root.findViewById<TextView>(R.id.tv_message)
        setupTextView(
            tvMessage,
            "“수고했어 오늘도”",
            kkubulim,
            DesignUtils.getScaledTextSize(context, 20f),
            applyShadow = true,
            lineSpacingMultiplier = null // 행간 Auto
        )
        applyTextShadow(tvMessage)

        // 로고 설정 (우측 하단, 여백 16)
        val ivLogo = root.findViewById<ImageView>(R.id.iv_logo)
        ivLogo?.apply {
            visibility = if (showLogo) View.VISIBLE else View.GONE

            layoutParams = (layoutParams as? ConstraintLayout.LayoutParams)?.apply {
                val logoSize = DesignUtils.dpToPxInt(context, 36f) // 36*36px
                width = logoSize
                height = logoSize
                // 직접 마진 설정
                bottomMargin = DesignUtils.dpToPxInt(context, 16f)
                marginEnd = DesignUtils.dpToPxInt(context, 16f)
            }
        }

        // 상단 컨테이너 (시간+날짜): 상단 여백 24px
        val topContainer = root.findViewById<View>(R.id.top_container)
        setMargin(topContainer, top = DesignUtils.dpToPxInt(context, 24f))

        // 하단 문구: 하단 여백 16px
        setMargin(tvMessage, bottom = DesignUtils.dpToPxInt(context, 16f))

        // 시간/날짜 사이 간격 (Gap: 0)
        setMargin(tvDate, top = 0)
    }

    /**
     * Moody 5 템플릿 데이터 바인딩
     */
    private fun bindMoody5(showLogo: Boolean, timestamp: Long) {
        val ongleip = loadFont(R.font.ongleip_parkdahyeon) // 온글잎 박다현체

        // 날짜 설정 (YYYY. MM. DD)
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        setupTextView(
            tvDate,
            formatDate("yyyy. MM. dd", timestamp),
            ongleip,
            DesignUtils.getScaledTextSize(context, 24f),
            applyShadow = true,
            lineSpacingMultiplier = 1.0f // 행간
        )

        // 시간 설정 (HH:mm)
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        setupTextView(
            tvTime,
            formatDate("HH:mm", timestamp),
            ongleip,
            DesignUtils.getScaledTextSize(context, 40f),
            applyShadow = true,
            lineSpacingMultiplier = 1.0f
        )

        // 원형 배경 설정
        val ivCircle = root.findViewById<ImageView>(R.id.iv_circle_border)
        ivCircle?.layoutParams = (ivCircle?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            val circleSize = DesignUtils.dpToPxInt(context, 300f) // 300*300px
            width = circleSize
            height = circleSize
        }

        // 로고 설정
        val ivLogo = root.findViewById<ImageView>(R.id.iv_stampic_logo)
        ivLogo?.apply {
            visibility = if (showLogo) View.VISIBLE else View.GONE
            (layoutParams as? ConstraintLayout.LayoutParams)?.apply {
                // 좌측 하단 여백 16
                leftMargin = DesignUtils.dpToPxInt(context, 16f)
                bottomMargin = DesignUtils.dpToPxInt(context, 16f)
            }
        }

        // Gap: 0px (날짜와 시간 사이 여백 제거)
        setMargin(tvTime, top = 0)
    }

    /**
     * Moody 6 템플릿 데이터 바인딩
     */
    private fun bindMoody6(showLogo: Boolean, timestamp: Long) {
        val monofettFont = loadFont(R.font.monofett_regular)

        // 날짜 설정 (yyyy.mm.dd)
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        setupTextView(
            tvDate,
            formatDate("yyyy.MM.dd", timestamp),
            monofettFont,
            DesignUtils.getScaledTextSize(context, 25f),
            applyShadow = true,
            lineSpacingMultiplier = null // 행간 따로 설정 X
        )

        // 시간 설정 (HH:mm)
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        setupTextView(
            tvTime,
            formatDate("HH:mm", timestamp),
            monofettFont,
            DesignUtils.getScaledTextSize(context, 25f),
            applyShadow = true,
            lineSpacingMultiplier = null
        )

        // 로고 설정
        val ivLogo = root.findViewById<ImageView>(R.id.iv_stampic_logo)
        ivLogo?.apply {
            visibility = if (showLogo) View.VISIBLE else View.GONE

            layoutParams = (layoutParams as? ConstraintLayout.LayoutParams)?.apply {
                val logoSize = DesignUtils.dpToPxInt(context, 38f)
                width = logoSize
                height = logoSize
                topMargin = DesignUtils.dpToPxInt(context, 16f)
                marginEnd = DesignUtils.dpToPxInt(context, 16f)
            }
        }
    }
}