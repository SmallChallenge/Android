package com.project.stampy.template

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
            "active_4" -> bindActive4(showLogo, photoTakenAtTimestamp)
            "active_5" -> bindActive5(showLogo, photoTakenAtTimestamp)
            "active_6" -> bindActive6(showLogo, photoTakenAtTimestamp)
            "active_7" -> bindActive7(showLogo, photoTakenAtTimestamp)
            "active_8" -> bindActive8(showLogo, photoTakenAtTimestamp)
        }
    }

    /**
     * Active 1 템플릿 데이터 바인딩
     */
    private fun bindActive1(showLogo: Boolean, timestamp: Long) {
        val gihugwigiFont = loadFont(R.font.gihugwigi1990)  // 기후위기 폰트 로드

        // 시간 설정 (HH:mm), 행간 Auto
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val currentTime = formatDate("HH:mm", timestamp)
        setupTextView(
            tvTime,
            currentTime,
            gihugwigiFont,
            DesignUtils.getScaledTextSize(context, 50f),
            applyShadow = true,
            lineSpacingMultiplier = null
        )

        // 날짜 설정 (E, d MMM), 행간 Auto
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        val rawDate = formatDate("E, d MMM", timestamp, Locale.US)
        // 첫 글자만 대문자로 변환 (예: Wed, 8 Nov)
        val formattedDate = rawDate.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
        }

        setupTextView(
            tvDate,
            formattedDate,
            gihugwigiFont,
            DesignUtils.getScaledTextSize(context, 16f),
            applyShadow = true,
            lineSpacingMultiplier = null
        )

        // 로고
        val ivLogo = root.findViewById<ImageView>(R.id.iv_stampic_logo)
        ivLogo?.visibility = if (showLogo) View.VISIBLE else View.GONE

        // 여백 조정
        setMargin(
            ivLogo,
            bottom = DesignUtils.dpToPxInt(context, 11f) // 로고: 하단 16px (Blur 5 설정이 있어서 5dp 뺀 11f임)
        )
    }

    /**
     * Active 2 템플릿 데이터 바인딩
     */
    private fun bindActive2(showLogo: Boolean, timestamp: Long) {
        val partialSansKrFont = loadFont(R.font.partial_sans_kr)    // Partial Sans KR 폰트 로드
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
            partialSansKrFont,
            DesignUtils.getScaledTextSize(context, 28f),
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
            partialSansKrFont,
            DesignUtils.getScaledTextSize(context, 18f),
            applyShadow = true,
            lineSpacingMultiplier = null  // 행간 Auto
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

        // 날짜 설정 (E, d MMM), 행간 100%
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
            DesignUtils.getScaledTextSize(context, 14f),
            applyShadow = true,
            lineSpacingMultiplier = 1.0f
        )

        // 시간 설정 (HH\nmm), 행간 100%
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val timeText = String.format("%02d\n%02d", hour, minute)

        tvTime?.apply {
            text = timeText
            ericaOne?.let { typeface = it }
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, DesignUtils.getScaledTextSize(context, 70f))
            setLineSpacing(0f, 0.7f) // 행간 100%로 변경해야함.. 임시방편으로 행간 좁게(0.7 배수) 설정해둠
            applyTextShadow(this)
        }

        // TODAY DONE 텍스트 (행간 100%)
        val tvText = root.findViewById<TextView>(R.id.tv_text)
        setupTextView(
            tvText,
            "TODAY DONE",
            pretendardMedium,
            DesignUtils.getScaledTextSize(context, 14f),
            applyShadow = true,
            lineSpacingMultiplier = 1.0f
        )

        // 로고
        root.findViewById<android.widget.ImageView>(R.id.iv_stampic_logo)?.apply {
            visibility = if (showLogo) View.VISIBLE else View.GONE
        }
    }

    /**
     * Active 4 템플릿 데이터 바인딩
     */
    private fun bindActive4(showLogo: Boolean, timestamp: Long) {
        val partialSans = loadFont(R.font.partial_sans_kr) // 파셜산스 폰트 로드
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 시간 설정 (오전/오후 h:mm)
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "오전" else "오후"
        val hour = calendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val minute = String.format("%02d", calendar.get(Calendar.MINUTE))

        setupTextView(
            tvTime,
            "$amPm $hour:$minute",
            partialSans,
            DesignUtils.getScaledTextSize(context, 28f),
            applyShadow = false,
            lineSpacingMultiplier = null // 행간 Auto
        )
        applyTextShadow(tvTime)

        // 날짜 설정 (YYYY. MM. DD (요일))
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        setupTextView(
            tvDate,
            formatDate("yyyy. MM. dd (${getDayOfWeekKorean(calendar)})", timestamp),
            partialSans,
            DesignUtils.getScaledTextSize(context, 12f),
            applyShadow = false,
            lineSpacingMultiplier = null
        )
        applyTextShadow(tvDate)

        // 로고
        val ivLogo = root.findViewById<ImageView>(R.id.iv_logo)
        ivLogo?.apply {
            visibility = if (showLogo) View.VISIBLE else View.GONE

            layoutParams = (layoutParams as? ConstraintLayout.LayoutParams)?.apply {
                // 로고 크기 조정 (예: 36dp)
                val logoSize = DesignUtils.dpToPxInt(context, 36f)
                width = logoSize
                height = logoSize
                bottomMargin = DesignUtils.dpToPxInt(context, 16f) // 하단 여백 16px
            }
        }

        // 날짜/시간 여백 및 간격 조정
        val topContainer = root.findViewById<View>(R.id.top_container)
        setMargin(topContainer, top = DesignUtils.dpToPxInt(context, 24f))

        // 시간/날짜 사이 간격 (Gap: 4)
        setMargin(tvDate, top = DesignUtils.dpToPxInt(context, 4f))
    }

    /**
     * Active 5 템플릿 데이터 바인딩
     */
    private fun bindActive5(showLogo: Boolean, timestamp: Long) {
        val partialSansFont = loadFont(R.font.partial_sans_kr)
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 날짜 설정 (YYYY. MM. DD. EEE (영문 약어 대문자))
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        val dateText = formatDate("yyyy. MM. dd. ", timestamp) +
                formatDate("EEE", timestamp, Locale.US).uppercase(Locale.US)

        setupTextView(
            tvDate,
            dateText,
            partialSansFont,
            DesignUtils.getScaledTextSize(context, 14f),
            applyShadow = true,
            lineSpacingMultiplier = 1.0f
        )

        // 시간 설정 (오전/오후 h:mm)
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "오전" else "오후"
        val hour = calendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val minute = String.format("%02d", calendar.get(Calendar.MINUTE))

        setupTextView(
            tvTime,
            "$amPm $hour:$minute",
            partialSansFont,
            DesignUtils.getScaledTextSize(context, 24f),
            applyShadow = true,
            lineSpacingMultiplier = 1.0f
        )

        // 중앙 상단 문구
        val tvMessage = root.findViewById<TextView>(R.id.tv_message)
        setupTextView(
            tvMessage,
            "JUST DO IT",
            partialSansFont,
            DesignUtils.getScaledTextSize(context, 14f),
            applyShadow = true,
            lineSpacingMultiplier = 1.0f
        )

        // 로고 설정: 우측 상단 (여백 16)
        val ivLogo = root.findViewById<ImageView>(R.id.iv_stampic_logo)
        ivLogo?.visibility = if (showLogo) View.VISIBLE else View.GONE

        // 여백 설정
        setMargin(tvDate, bottom = DesignUtils.dpToPxInt(context, 24f)) // 하단 전체 여백 24
        setMargin(tvTime, top = DesignUtils.dpToPxInt(context, 6f))    // 날짜와 시간 사이 Gap 6
        setMargin(tvMessage, top = DesignUtils.dpToPxInt(context, 14f)) // 상단 문구 여백 14
        setMargin(ivLogo, top = DesignUtils.dpToPxInt(context, 16f), end = DesignUtils.dpToPxInt(context, 16f))
    }

    /**
     * Active 6 템플릿 데이터 바인딩
     */
    private fun bindActive6(showLogo: Boolean, timestamp: Long) {
        val kerisFont = loadFont(R.font.keris_kedu) // 케리스 케듀체 폰트 로드
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 날짜 설정: YYYY.mm.dd.(요일)
        val tvDate = root.findViewById<StrokeTextView>(R.id.tv_date)
        val dateText = formatDate("yyyy.MM.dd.(${getDayOfWeekKorean(calendar)})", timestamp)

        setupTextView(
            tvDate,
            dateText,
            kerisFont,
            DesignUtils.getScaledTextSize(context, 16f),
            applyShadow = true,
            lineSpacingMultiplier = null // 행간 Auto
        )
        // 흰색 텍스트 + 검정 외곽선
        tvDate?.setStroke(DesignUtils.dpToPx(context, 2f), 0xFF000000.toInt())

        // 시간 설정: 오전/오후 h:mm
        val tvTime = root.findViewById<StrokeTextView>(R.id.tv_time)
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "오전" else "오후"
        val hour = calendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val minute = String.format("%02d", calendar.get(Calendar.MINUTE))

        setupTextView(
            tvTime,
            "$amPm $hour:$minute",
            kerisFont,
            DesignUtils.getScaledTextSize(context, 16f),
            applyShadow = true,
            lineSpacingMultiplier = null
        )
        // 흰색 텍스트 + 검정 외곽선
        tvTime?.setStroke(DesignUtils.dpToPx(context, 2f), 0xFF000000.toInt())

        // 중앙 하단 문구
        val tvMessage = root.findViewById<StrokeTextView>(R.id.tv_message)
        setupTextView(
            tvMessage,
            "오늘도 해냈다!",
            kerisFont,
            DesignUtils.getScaledTextSize(context, 20f),
            applyShadow = true,
            lineSpacingMultiplier = null
        )
        // 흰색 텍스트 + 검정 외곽선
        tvMessage?.setStroke(DesignUtils.dpToPx(context, 2f), 0xFF000000.toInt())

        // 로고
        val ivLogo = root.findViewById<ImageView>(R.id.iv_logo)
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

    /**
     * Active 7 템플릿 데이터 바인딩
     */
    private fun bindActive7(showLogo: Boolean, timestamp: Long) {
        val kerisFont = loadFont(R.font.keris_kedu) // 케리스 케듀체 폰트 로드
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 시간 설정: 오전/오후 h:mm (Size: 24, 행간 100%, 자간 0)
        val tvTime = root.findViewById<StrokeTextView>(R.id.tv_time)
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "오전" else "오후"
        val hour = calendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val minute = String.format("%02d", calendar.get(Calendar.MINUTE))

        setupTextView(
            tvTime,
            "$amPm $hour:$minute",
            kerisFont,
            DesignUtils.getScaledTextSize(context, 24f),
            applyShadow = true,
            lineSpacingMultiplier = 1.0f
        )
        // 흰색 텍스트 + 검정 외곽선 (이미지의 외곽선 효과 반영)
        tvTime?.setStroke(DesignUtils.dpToPx(context, 2f), 0xFF000000.toInt())

        // 날짜 설정: YYYY.mm.dd (요일) (Size: 16, 행간 100%, 자간 0)
        val tvDate = root.findViewById<StrokeTextView>(R.id.tv_date)
        val dateText = formatDate("yyyy.MM.dd (${getDayOfWeekKorean(calendar)})", timestamp)

        setupTextView(
            tvDate,
            dateText,
            kerisFont,
            DesignUtils.getScaledTextSize(context, 16f),
            applyShadow = true,
            lineSpacingMultiplier = 1.0f
        )
        tvDate?.setStroke(DesignUtils.dpToPx(context, 2f), 0xFF000000.toInt())

        // 로고 설정: 우측 하단 (여백 16)
        val ivLogo = root.findViewById<ImageView>(R.id.iv_logo)
        ivLogo?.apply {
            visibility = if (showLogo) View.VISIBLE else View.GONE

            (layoutParams as? ConstraintLayout.LayoutParams)?.apply {
                // 우측 하단 배치 및 여백 16px
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                topToTop = ConstraintLayout.LayoutParams.UNSET

                val margin16 = DesignUtils.dpToPxInt(context, 16f)
                setMargins(0, 0, margin16, margin16)
            }
        }

        // 간격 조정 (Gap : 6)
        // 날짜 텍스트의 상단 마진을 6으로 설정하여 시간과의 간격 유지
        setMargin(tvDate, top = DesignUtils.dpToPxInt(context, 6f))
    }

    /**
     * Active 8 템플릿 데이터 바인딩
     */
    private fun bindActive8(showLogo: Boolean, timestamp: Long) {
        val kerisFont = loadFont(R.font.keris_kedu) // RixInooAriDuri 폰트 로드
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 날짜 설정 (2줄로 구성)
        // 첫째 줄: YYYY년
        // 둘째 줄: mm월 dd일 (요일)
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        val year = formatDate("yyyy년", timestamp)
        val dayMonth = formatDate("MM월 dd일 (${getDayOfWeekKorean(calendar)})", timestamp)
        val dateText = "$year\n$dayMonth"

        setupTextView(
            tvDate,
            dateText,
            kerisFont,
            DesignUtils.getScaledTextSize(context, 18f),
            applyShadow = true,
            lineSpacingMultiplier = 1.3f // 행간 130%
        )

        // 시간 설정: 오전/오후 h:mm
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val amPm = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "오전" else "오후"
        val hour = calendar.get(Calendar.HOUR).let { if (it == 0) 12 else it }
        val minute = String.format("%02d", calendar.get(Calendar.MINUTE))

        setupTextView(
            tvTime,
            "$amPm $hour:$minute",
            kerisFont,
            DesignUtils.getScaledTextSize(context, 18f),
            applyShadow = true,
            lineSpacingMultiplier = 1.3f // 행간 130%
        )

        // 로고 설정: 우측 상단 (여백 16px)
        val ivLogo = root.findViewById<ImageView>(R.id.iv_logo)
        ivLogo?.apply {
            visibility = if (showLogo) View.VISIBLE else View.GONE

            (layoutParams as? ConstraintLayout.LayoutParams)?.apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                startToStart = ConstraintLayout.LayoutParams.UNSET

                val margin16 = DesignUtils.dpToPxInt(context, 16f)
                setMargins(0, margin16, margin16, 0)
            }
        }

        // 컨테이너 여백 (좌측 하단 여백 16)
        val bottomContainer = root.findViewById<View>(R.id.bottom_container)
        setMargin(
            bottomContainer,
            start = DesignUtils.dpToPxInt(context, 16f),
            bottom = DesignUtils.dpToPxInt(context, 16f)
        )
    }
}