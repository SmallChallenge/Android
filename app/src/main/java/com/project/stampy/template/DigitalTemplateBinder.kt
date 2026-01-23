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
            "digital_4" -> bindDigital4(showLogo, photoTakenAtTimestamp)
            "digital_5" -> bindDigital5(showLogo, photoTakenAtTimestamp)
        }
    }

    /**
     * Digital 1 템플릿 데이터 바인딩
     */
    private fun bindDigital1(showLogo: Boolean, timestamp: Long) {
        val suitExtraBold = loadFont(R.font.suit_extrabold) //suit ExtraBold 폰트 로드
        val suitHeavy = loadFont(R.font.suit_heavy) // suit Heavy 폰트 로드

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

        // 시간 첫째 자리 (행간 100%)
        root.findViewById<TextView>(R.id.tv_hour_1)?.apply {
            text = hour[0].toString()
            suitExtraBold?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
            setLineSpacing(0f, 1.0f)
            setShadowLayer(dropShadowRadius, dropShadowDx, dropShadowDy, dropShadowColor)
        }

        // 시간 둘째 자리 (행간 100%)
        root.findViewById<TextView>(R.id.tv_hour_2)?.apply {
            text = hour[1].toString()
            suitExtraBold?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
            setLineSpacing(0f, 1.0f)
            setShadowLayer(dropShadowRadius, dropShadowDx, dropShadowDy, dropShadowColor)
        }

        // 콜론 (행간 100%)
        root.findViewById<TextView>(R.id.tv_colon)?.apply {
            suitExtraBold?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
            setLineSpacing(0f, 1.0f)
        }

        // 분 첫째 자리 (행간 100%)
        root.findViewById<TextView>(R.id.tv_minute_1)?.apply {
            text = minute[0].toString()
            suitExtraBold?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
            setLineSpacing(0f, 1.0f)
            setShadowLayer(dropShadowRadius, dropShadowDx, dropShadowDy, dropShadowColor)
        }

        // 분 둘째 자리 (행간 100%)
        root.findViewById<TextView>(R.id.tv_minute_2)?.apply {
            text = minute[1].toString()
            suitExtraBold?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
            setLineSpacing(0f, 1.0f)
            setShadowLayer(dropShadowRadius, dropShadowDx, dropShadowDy, dropShadowColor)
        }

        // 날짜
        root.findViewById<TextView>(R.id.tv_date)?.apply {
            text = formatDate("yyyy.MM.dd", timestamp)
            suitHeavy?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, scaledDateSize)
            setShadowLayer(
                DesignUtils.dpToPx(context, 5f),
                0f,
                0f,
                0x73000000.toInt()
            )
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

        // 시간 설정 (HH:mm AM/PM), 행간 Auto
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
            DesignUtils.getScaledTextSize(context, 50f),
            applyShadow = true,
            lineSpacingMultiplier = null  // 행간 Auto
        )

        // 날짜 설정 (December DDth YYYY), 행간 Auto
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
            DesignUtils.getScaledTextSize(context, 24f),
            applyShadow = true,
            lineSpacingMultiplier = null  // 행간 Auto
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

        // 날짜 설정 (YYYY년M월D일(요일)), 행간 Auto
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = getDayOfWeekKorean(calendar)
        val dateText = String.format("%d년%d월%d일(%s)", year, month, day, dayOfWeek)

        setupTextView(
            tvDate,
            dateText,
            dunggeunmo,
            DesignUtils.getScaledTextSize(context, 28f),
            applyShadow = true,
            lineSpacingMultiplier = null  // 행간 Auto
        )

        // 시간 설정 (오전/오후 HH:mm), 행간 Auto
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
            DesignUtils.getScaledTextSize(context, 28f),
            applyShadow = true,
            lineSpacingMultiplier = null  // 행간 Auto
        )

        // 로고(그림자 없음)
        setLogoVisibility(R.id.iv_stampic_logo, showLogo)
    }

    /**
     * Digital 4 템플릿 데이터 바인딩
     */
    private fun bindDigital4(showLogo: Boolean, timestamp: Long) {
        val suitHeavy = loadFont(R.font.suit_heavy)
        val suitExtraBold = loadFont(R.font.suit_extrabold)
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 날짜 설정 ("yyyy.MM.dd")
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        setupTextView(
            tvDate,
            formatDate("yyyy.MM.dd", timestamp),
            suitExtraBold,
            DesignUtils.getScaledTextSize(context, 20f),
            applyShadow = false
        )
        applyTextShadow(tvDate)

        // 시간 설정 ("a h:mm")
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val timeText = formatDate("a h:mm", timestamp, Locale.US).lowercase()
        setupTextView(
            tvTime,
            timeText,
            suitHeavy,
            DesignUtils.getScaledTextSize(context, 32f),
            applyShadow = false
        )
        applyTextShadow(tvTime)

        // REC 아이콘 (상 32, 좌 32)
        val ivRec = root.findViewById<ImageView>(R.id.iv_rec)
        setMargin(ivRec, top = DesignUtils.dpToPxInt(context, 32f), start = DesignUtils.dpToPxInt(context, 32f))

        // 배터리 아이콘 (상 26, 우 32)
        val ivBattery = root.findViewById<ImageView>(R.id.iv_battery)
        setMargin(ivBattery, top = DesignUtils.dpToPxInt(context, 26f), end = DesignUtils.dpToPxInt(context, 32f))

        // 날짜/시간 컨테이너 (하 32, 좌 32)
        val bottomStartContainer = root.findViewById<LinearLayout>(R.id.bottom_start_container)
        setMargin(bottomStartContainer, bottom = DesignUtils.dpToPxInt(context, 32f), start = DesignUtils.dpToPxInt(context, 32f))

        // 로고 (하 32, 우 32)
        val ivLogo = root.findViewById<ImageView>(R.id.iv_logo)
        ivLogo?.apply {
            visibility = if (showLogo) View.VISIBLE else View.GONE
            setMargin(this, bottom = DesignUtils.dpToPxInt(context, 32f), end = DesignUtils.dpToPxInt(context, 32f))
        }

        // 날짜와 시간 사이 간격 (Gap: 0)
        setMargin(tvTime, top = 0)
    }

    /**
     * Digital 5 템플릿 데이터 바인딩
     */
    private fun bindDigital5(showLogo: Boolean, timestamp: Long) {
        val cafe24Font = loadFont(R.font.cafe24_pro_up) // 카페 24 프로 업 폰트
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 날짜 설정 (yyyy.mm.dd)
        val tvDate = root.findViewById<StrokeTextView>(R.id.tv_date)
        setupTextView(
            tvDate,
            formatDate("yyyy.MM.dd", timestamp),
            cafe24Font,
            DesignUtils.getScaledTextSize(context, 20f),
            applyShadow = false, // 기본 그림자 대신 커스텀 외곽선 적용
            lineSpacingMultiplier = null // 행간 Auto
        )
        tvDate?.setStroke(DesignUtils.dpToPx(context, 2f), 0xFF000000.toInt())

        // 시간 설정 (am/pm hh:mm)
        val tvTime = root.findViewById<StrokeTextView>(R.id.tv_time)
        val timeText = formatDate("a hh:mm", timestamp, Locale.US).lowercase(Locale.US)
        setupTextView(
            tvTime,
            timeText,
            cafe24Font,
            DesignUtils.getScaledTextSize(context, 20f),
            applyShadow = false,
            lineSpacingMultiplier = null
        )
        tvTime?.setStroke(DesignUtils.dpToPx(context, 2f), 0xFF000000.toInt())

        // 로고 설정
        val ivLogo = root.findViewById<ImageView>(R.id.iv_stampic_logo)
        ivLogo?.visibility = if (showLogo) View.VISIBLE else View.GONE
    }
}