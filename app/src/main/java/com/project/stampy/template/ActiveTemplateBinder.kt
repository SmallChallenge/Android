package com.project.stampy.template

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.TextView
import com.project.stampy.R
import java.util.*

/**
 * Active 카테고리 템플릿 바인더
 */
class ActiveTemplateBinder(
    context: Context,
    root: View
) : TemplateBinderBase(context, root) {

    override fun bind(template: Template, showLogo: Boolean, photoTakenAtTimestamp: Long) {
        when (template.id) {
            "active_1" -> bindActive1(showLogo, photoTakenAtTimestamp)
            "active_2" -> bindActive2(showLogo, photoTakenAtTimestamp)
            "active_3" -> bindActive3(showLogo, photoTakenAtTimestamp)
        }
    }

    /**
     * Active 1 템플릿 데이터 바인딩
     */
    private fun bindActive1(showLogo: Boolean, timestamp: Long) {
        val gihugwigiFont = loadFont(R.font.gihugwigi1990)  // 기후위기 폰트 로드

        // 시간 설정 (HH:mm)
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val currentTime = formatDate("HH:mm", timestamp)
        setupTextView(
            tvTime,
            currentTime,
            gihugwigiFont,
            DesignUtils.getScaledTextSize(context, 50f)
        )

        // 날짜 설정 (E, d MMM)
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        val currentDate = formatDate("E, d MMM", timestamp, Locale.US).uppercase(Locale.US)
        setupTextView(
            tvDate,
            currentDate,
            gihugwigiFont,
            DesignUtils.getScaledTextSize(context, 16f)
        )

        // 로고
        setLogoVisibility(R.id.iv_stampic_logo, showLogo)

        // 여백 조정
        setMargin(
            root.findViewById(R.id.iv_stampic_logo),
            bottom = DesignUtils.dpToPxInt(context, 16f) // 로고: 아래 16px
        )
    }

    /**
     * Active 2 템플릿 데이터 바인딩
     */
    private fun bindActive2(showLogo: Boolean, timestamp: Long) {
        val partialSansKrFont = loadFont(R.font.partial_sans_kr)    // Partial Sans KR 폰트 로드
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
            partialSansKrFont,
            DesignUtils.getScaledTextSize(context, 28f)
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
            partialSansKrFont,
            DesignUtils.getScaledTextSize(context, 18f)
        )

        // 로고
        setLogoVisibility(R.id.iv_stampic_logo, showLogo)

        // 여백 조정
        setMargin(
            root.findViewById(R.id.iv_stampic_logo),
            top = DesignUtils.dpToPxInt(context, 16f)   // 로고: 상단 16px
        )
        setMargin(
            tvDate,
            bottom = DesignUtils.dpToPxInt(context, 24f)    // 날짜: 하단 24px
        )
    }

    /**
     * Active 3 템플릿 데이터 바인딩
     */
    private fun bindActive3(showLogo: Boolean, timestamp: Long) {
        val pretendardMedium = loadFont(R.font.pretendard_medium)   // Pretendard Medium 폰트 로드
        val ericaOne = loadFont(R.font.erica_one)   // Erica One 폰트 로드
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 날짜 설정 (E, d MMM)
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        val dayOfWeek = getDayOfWeekEnglish(calendar) // 요일 (영문 약어)
        val day = calendar.get(Calendar.DAY_OF_MONTH) // 일 (숫자)

        val month = when (calendar.get(Calendar.MONTH)) { // 월 (영문 약어)
            Calendar.JANUARY -> "Jan"
            Calendar.FEBRUARY -> "Feb"
            Calendar.MARCH -> "Mar"
            Calendar.APRIL -> "Apr"
            Calendar.MAY -> "May"
            Calendar.JUNE -> "Jun"
            Calendar.JULY -> "Jul"
            Calendar.AUGUST -> "Aug"
            Calendar.SEPTEMBER -> "Sept"
            Calendar.OCTOBER -> "Oct"
            Calendar.NOVEMBER -> "Nov"
            Calendar.DECEMBER -> "Dec"
            else -> ""
        }

        val dateText = "$dayOfWeek, $day $month"
        setupTextView(
            tvDate,
            dateText,
            pretendardMedium,
            DesignUtils.getScaledTextSize(context, 14f)
        )

        // 시간 설정 (HH\nmm)
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timeText = String.format("%02d\n%02d", hour, minute)

        setupTextView(
            tvTime,
            timeText,
            ericaOne,
            DesignUtils.getScaledTextSize(context, 70f)
        )
        tvTime?.setLineSpacing(0f, 0.7f) // 행간 좁게 (0.7 배수)

        // TODAY DONE 텍스트
        val tvText = root.findViewById<TextView>(R.id.tv_text)
        setupTextView(
            tvText,
            "TODAY DONE",
            pretendardMedium,
            DesignUtils.getScaledTextSize(context, 14f)
        )

        // 로고 (그림자 포함)
        root.findViewById<android.widget.ImageView>(R.id.iv_stampic_logo)?.apply {
            visibility = if (showLogo) View.VISIBLE else View.GONE

            // ImageView에 그림자 효과 (elevation 사용)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                elevation = DesignUtils.dpToPx(context, 4f)
                outlineProvider = android.view.ViewOutlineProvider.BOUNDS
            }
        }
    }
}