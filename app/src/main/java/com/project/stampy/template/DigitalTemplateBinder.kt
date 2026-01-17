package com.project.stampy.template

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.project.stampy.R
import java.util.*

/**
 * Digital 카테고리 템플릿 바인더
 */
class DigitalTemplateBinder(
    context: Context,
    root: View
) : TemplateBinderBase(context, root) {

    override fun bind(template: Template, showLogo: Boolean, photoTakenAtTimestamp: Long) {
        when (template.id) {
            "digital_1" -> bindDigital1(showLogo, photoTakenAtTimestamp)
            "digital_2" -> bindDigital2(showLogo, photoTakenAtTimestamp)
            "digital_3" -> bindDigital3(showLogo, photoTakenAtTimestamp)
        }
    }

    /**
     * Digital 1 템플릿 데이터 바인딩
     */
    private fun bindDigital1(showLogo: Boolean, timestamp: Long) {
        val suitExtraBold = loadFont(R.font.suit_extrabold)
        val suitBold = loadFont(R.font.suit_bold)

        val currentTime = formatDate("HH:mm", timestamp)
        val timeParts = currentTime.split(":")
        val hour = timeParts[0]
        val minute = timeParts[1]

        // 375px 기준 가변 크기 계산
        val scaledTextSize = DesignUtils.getScaledTextSize(context, 50f)
        val scaledDateSize = DesignUtils.getScaledTextSize(context, 24f)

        // Drop Shadow: x3 y3 blur10 #000000 40%
        val dropShadowRadius = DesignUtils.dpToPx(context, 10f)
        val dropShadowDx = DesignUtils.dpToPx(context, 3f)
        val dropShadowDy = DesignUtils.dpToPx(context, 3f)
        val dropShadowColor = 0x66000000.toInt() // 40%

        // 시간 첫째 자리
        root.findViewById<TextView>(R.id.tv_hour_1)?.apply {
            text = hour[0].toString()
            suitExtraBold?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
            setShadowLayer(dropShadowRadius, dropShadowDx, dropShadowDy, dropShadowColor)
        }

        // 시간 둘째 자리
        root.findViewById<TextView>(R.id.tv_hour_2)?.apply {
            text = hour[1].toString()
            suitExtraBold?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
            setShadowLayer(dropShadowRadius, dropShadowDx, dropShadowDy, dropShadowColor)
        }

        // 콜론
        root.findViewById<TextView>(R.id.tv_colon)?.apply {
            suitExtraBold?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
        }

        // 분 첫째 자리
        root.findViewById<TextView>(R.id.tv_minute_1)?.apply {
            text = minute[0].toString()
            suitExtraBold?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
            setShadowLayer(dropShadowRadius, dropShadowDx, dropShadowDy, dropShadowColor)
        }

        // 분 둘째 자리
        root.findViewById<TextView>(R.id.tv_minute_2)?.apply {
            text = minute[1].toString()
            suitExtraBold?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
            setShadowLayer(dropShadowRadius, dropShadowDx, dropShadowDy, dropShadowColor)
        }

        // 날짜
        root.findViewById<TextView>(R.id.tv_date)?.apply {
            text = formatDate("yyyy.MM.dd", timestamp)
            suitBold?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledDateSize)
        }

        // 로고
        setLogoVisibility(R.id.iv_stampic_logo, showLogo)

        // 여백 조정 (375px 기준 가변)
        adjustDigital1Margins()
    }

    /**
     * Digital 1 템플릿 여백 조정 (375px 기준 가변)
     */
    private fun adjustDigital1Margins() {
        // 375px 기준 가변 크기 계산
        val boxWidth = DesignUtils.dpToPxInt(context, 45f)
        val boxHeight = DesignUtils.dpToPxInt(context, 60f)
        val gap = DesignUtils.dpToPxInt(context, 6f)

        // 시간 박스 크기
        // 시간 첫째 자리
        root.findViewById<TextView>(R.id.tv_hour_1)?.layoutParams =
            root.findViewById<TextView>(R.id.tv_hour_1)?.layoutParams?.apply {
                width = boxWidth
                height = boxHeight
            }

        // 시간 둘째 자리
        root.findViewById<TextView>(R.id.tv_hour_2)?.layoutParams =
            (root.findViewById<TextView>(R.id.tv_hour_2)?.layoutParams as? LinearLayout.LayoutParams)?.apply {
                width = boxWidth
                height = boxHeight
                marginStart = gap
            }

        // 콜론
        root.findViewById<TextView>(R.id.tv_colon)?.layoutParams =
            (root.findViewById<TextView>(R.id.tv_colon)?.layoutParams as? LinearLayout.LayoutParams)?.apply {
                marginStart = gap
                marginEnd = gap
            }

        // 분 첫째 자리
        root.findViewById<TextView>(R.id.tv_minute_1)?.layoutParams =
            root.findViewById<TextView>(R.id.tv_minute_1)?.layoutParams?.apply {
                width = boxWidth
                height = boxHeight
            }

        // 분 둘째 자리
        root.findViewById<TextView>(R.id.tv_minute_2)?.layoutParams =
            (root.findViewById<TextView>(R.id.tv_minute_2)?.layoutParams as? LinearLayout.LayoutParams)?.apply {
                width = boxWidth
                height = boxHeight
                marginStart = gap
            }

        // 시간 컨테이너 - 상단에서 70px (가변)
        val timeContainer = root.findViewById<LinearLayout>(R.id.time_container)
        (timeContainer?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            topMargin = DesignUtils.dpToPxInt(context, 70f)
            timeContainer.layoutParams = this
        }

        // 날짜 - 하단 여백 16px (가변)
        setMargin(
            root.findViewById(R.id.tv_date),
            bottom = DesignUtils.dpToPxInt(context, 16f)
        )

        // 로고 - 상단 여백 16px (가변) - 이미 XML에서 설정되어 있지만 확실하게
        setMargin(
            root.findViewById(R.id.iv_stampic_logo),
            top = DesignUtils.dpToPxInt(context, 16f)
        )
    }

    /**
     * Digital 2 템플릿 데이터 바인딩
     */
    private fun bindDigital2(showLogo: Boolean, timestamp: Long) {
        val dunggeunmoFont = loadFont(R.font.dunggeunmo)    // DungGeunMo 폰트 로드
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 시간 설정 (HH:mm AM/PM)
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val timeText = String.format("%02d:%02d %s", displayHour, minute, amPm)

        setupTextView(
            tvTime,
            timeText,
            dunggeunmoFont,
            DesignUtils.getScaledTextSize(context, 50f)
        )

        // 날짜 설정 (December DDth YYYY)
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val daySuffix = getDaySuffix(day)
        val dateText = "$month ${day}${daySuffix} $year"

        setupTextView(
            tvDate,
            dateText,
            dunggeunmoFont,
            DesignUtils.getScaledTextSize(context, 24f)
        )

        // 로고
        setLogoVisibility(R.id.iv_stampic_logo, showLogo)

        // 여백 조정
        setMargin(
            tvTime,
            top = DesignUtils.dpToPxInt(context, 40f)   // 시간: 상단 40px
        )
        setMargin(
            root.findViewById(R.id.iv_stampic_logo),
            bottom = DesignUtils.dpToPxInt(context, 16f)    // 로고: 하단 16px
        )
    }

    /**
     * Digital 3 템플릿 데이터 바인딩
     */
    private fun bindDigital3(showLogo: Boolean, timestamp: Long) {
        val dunggeunmo = loadFont(R.font.dunggeunmo)    // DungGeunMo 폰트 로드
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 날짜 설정 (YYYY년MM월DD일(요일))
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = getDayOfWeekKorean(calendar)
        val dateText = String.format("%d년%02d월%02d일(%s)", year, month, day, dayOfWeek)

        setupTextView(
            tvDate,
            dateText,
            dunggeunmo,
            DesignUtils.getScaledTextSize(context, 28f)
        )

        // 시간 설정 (오전/오후 HH:mm)
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val amPm = if (hour < 12) "오전" else "오후"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val timeText = String.format("%s%02d:%02d", amPm, displayHour, minute)

        setupTextView(
            tvTime,
            timeText,
            dunggeunmo,
            DesignUtils.getScaledTextSize(context, 28f)
        )

        // 로고(그림자 없음)
        setLogoVisibility(R.id.iv_stampic_logo, showLogo)
    }
}