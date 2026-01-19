package com.project.stampy.template

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.project.stampy.R
import java.util.*

/**
 * Basic 카테고리 템플릿 바인더
 */
class BasicTemplateBinder(
    context: Context,
    root: View
) : TemplateBinderBase(context, root) {

    override fun bind(template: Template, showLogo: Boolean, photoTakenAtTimestamp: Long) {
        when (template.id) {
            "basic_1" -> bindBasic1(showLogo, photoTakenAtTimestamp)
            "basic_2" -> bindBasic2(showLogo, photoTakenAtTimestamp)
            "basic_3" -> bindBasic3(showLogo, photoTakenAtTimestamp)
        }
    }

    /**
     * Basic 1 템플릿 데이터 바인딩
     */
    private fun bindBasic1(showLogo: Boolean, timestamp: Long) {
        // Pretendard Medium 폰트 로드
        val pretendardMedium = loadFont(R.font.pretendard_medium)

        // 시간 설정 (행간 100%)
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val currentTime = formatDate("HH:mm", timestamp)
        setupTextView(
            tvTime,
            currentTime,
            pretendardMedium,
            DesignUtils.getScaledTextSize(context, 55f),
            applyShadow = true,
            lineSpacingMultiplier = 1.0f  // 행간 100%
        )
        // 자간 -2%는 이미 XML에 letterSpacing="-0.02"로 설정됨

        // 날짜 + Stampic 설정 (행간 100%)
        val tvDateStampic = root.findViewById<TextView>(R.id.tv_date_stampic)
        val currentDate = formatDate("yyyy.MM.dd", timestamp)
        setupTextView(
            tvDateStampic,
            "$currentDate • Stampic",
            pretendardMedium,
            // 디자인 가이드 15 (375 기준) → 기기에 맞춰 가변
            DesignUtils.getScaledTextSize(context, 15f),
            applyShadow = true,
            lineSpacingMultiplier = 1.0f  // 행간 100%
        )
        // 자간 -2%는 이미 XML에 letterSpacing="-0.02"로 설정됨

        // 로고 표시/숨김 및 크기 조정
        val ivLogo = root.findViewById<ImageView>(R.id.iv_logo)
        ivLogo?.apply {
            visibility = if (showLogo) View.VISIBLE else View.GONE

            // 로고 크기를 기기에 맞춰 조정 (디자인 가이드에 따라 적절한 크기 설정)
            // 38px 기준으로 가변
            layoutParams = layoutParams?.apply {
                val logoSize = DesignUtils.dpToPxInt(context, 38f)
                width = logoSize
                height = logoSize
            }
        }

        // 여백 조정
        setMargin(tvTime, top = DesignUtils.dpToPxInt(context, 24f)) // 시간: 상단 여백 24px
        setMargin(tvDateStampic, top = DesignUtils.dpToPxInt(context, 8f)) // 날짜+Stampic: 시간 아래 8px
        setMargin(
            ivLogo,
            bottom = DesignUtils.dpToPxInt(context, 16f), // 로고: 오른쪽 16px, 아래 16px
            end = DesignUtils.dpToPxInt(context, 16f)
        )
    }

    /**
     * Basic 2 템플릿 데이터 바인딩
     */
    private fun bindBasic2(showLogo: Boolean, timestamp: Long) {
        val suitHeavy = loadFont(R.font.suit_heavy) // SUIT Heavy 폰트 로드
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 날짜 설정 (YYYY년 MM월 DD일 (요일)), 행간 Auto
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = getDayOfWeekKorean(calendar)
        val dateText = "${year}년 ${month}월 ${day}일 (${dayOfWeek})"

        setupTextView(
            tvDate,
            dateText,
            suitHeavy,
            DesignUtils.getScaledTextSize(context, 24f),
            applyShadow = true,
            lineSpacingMultiplier = null  // 행간 Auto
        )

        // 시간 설정 (오전/오후 h:mm),행간 Auto
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val amPm = if (hour < 12) "오전" else "오후"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val timeText = String.format("%s %d:%02d", amPm, displayHour, minute)

        setupTextView(
            tvTime,
            timeText,
            suitHeavy,
            DesignUtils.getScaledTextSize(context, 24f),
            applyShadow = true,
            lineSpacingMultiplier = null  // 행간 Auto
        )

        // 아이콘
        val ivLogoIcon = root.findViewById<ImageView>(R.id.iv_logo_icon)
        ivLogoIcon?.apply {
            visibility = if (showLogo) View.VISIBLE else View.GONE

            // 아이콘 크기를 기기에 맞춰 조정 (38dp 기준)
            layoutParams = layoutParams?.apply {
                val iconSize = DesignUtils.dpToPxInt(context, 38f)
                width = iconSize
                height = iconSize
            }
        }

        // 여백 조정
        setMargin(
            ivLogoIcon, // 아이콘: 상단 16px, 우측 16px
            top = DesignUtils.dpToPxInt(context, 16f),
            end = DesignUtils.dpToPxInt(context, 16f)
        )
        // 날짜/시간 컨테이너: 좌측 24px, 하단 24px
        val datetimeContainer = root.findViewById<LinearLayout>(R.id.datetime_container)
        setMargin(
            datetimeContainer,
            bottom = DesignUtils.dpToPxInt(context, 24f),
            start = DesignUtils.dpToPxInt(context, 24f)
        )
    }

    /**
     * Basic 3 템플릿 데이터 바인딩
     */
    private fun bindBasic3(showLogo: Boolean, timestamp: Long) {
        val suitHeavy = loadFont(R.font.suit_heavy) // SUIT Heavy 폰트 로드
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }

        // 시간 설정 (오전/오후 HH:mm), 행간 Auto
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val amPm = if (hour < 12) "오전" else "오후"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val timeText = String.format("%s %02d:%02d", amPm, displayHour, minute)

        setupTextView(
            tvTime,
            timeText,
            suitHeavy,
            DesignUtils.getScaledTextSize(context, 40f),
            applyShadow = true,
            lineSpacingMultiplier = null  // 행간 Auto
        )

        // 날짜 설정 (YYYY년 MM월 DD일 (요일)), 행간 Auto
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = getDayOfWeekKorean(calendar)
        val dateText = "${year}년 ${month}월 ${day}일 (${dayOfWeek})"

        setupTextView(
            tvDate,
            dateText,
            suitHeavy,
            DesignUtils.getScaledTextSize(context, 20f),
            applyShadow = true,
            lineSpacingMultiplier = null  // 행간 Auto
        )

        // 로고
        setLogoVisibility(R.id.iv_stampic_logo, showLogo)

        // 여백 조정(로고: 하단 16px)
        setMargin(
            root.findViewById(R.id.iv_stampic_logo),
            bottom = DesignUtils.dpToPxInt(context, 16f)
        )
    }
}