package com.project.stampy.template

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.project.stampy.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * 템플릿 뷰 클래스
 * 선택된 템플릿을 동적으로 inflate하고 데이터 바인딩
 */
class TemplateView(context: Context) : FrameLayout(context) {

    private var currentTemplate: Template? = null
    private var templateRootView: View? = null

    /**
     * 템플릿 적용
     */
    fun applyTemplate(template: Template, showLogo: Boolean = true) {
        // 기존 뷰 제거
        templateRootView?.let { removeView(it) }

        // 새 템플릿 inflate
        val inflater = LayoutInflater.from(context)
        templateRootView = inflater.inflate(template.layoutResId, this, false)

        // 뷰 추가
        addView(templateRootView)

        currentTemplate = template

        // 템플릿별 데이터 바인딩
        when (template.id) {
            "basic_1" -> bindBasic1Template(showLogo)
            "basic_2" -> bindBasic2Template(showLogo)
            "moody_1" -> bindMoody1Template(showLogo)
            "moody_2" -> bindMoody2Template(showLogo)
            "active_1" -> bindActive1Template(showLogo)
            "digital_1" -> bindDigital1Template(showLogo)
        }
    }

    /**
     * Basic 1 템플릿 데이터 바인딩
     */
    private fun bindBasic1Template(showLogo: Boolean) {
        templateRootView?.let { root ->
            // Pretendard Medium 폰트 로드
            val pretendardMedium = ResourcesCompat.getFont(context, R.font.pretendard_medium)

            // === 시간 설정 ===
            val tvTime = root.findViewById<TextView>(R.id.tv_time)
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            tvTime?.apply {
                text = currentTime
                typeface = pretendardMedium
                setTextSize(TypedValue.COMPLEX_UNIT_SP, DesignUtils.getScaledTextSize(this@TemplateView.context, 55f))
                // 행간 100%
                setLineSpacing(0f, 1.0f)
                // 자간 -2%는 이미 XML에 letterSpacing="-0.02"로 설정됨

                // 텍스트 그림자: x=0, y=0, 흐림=5px, #000000 45%
                setShadowLayer(
                    DesignUtils.dpToPx(this@TemplateView.context, 5f),
                    0f,
                    0f,
                    0x73000000.toInt()
                )
            }

            // === 날짜 + Stampic 설정 ===
            val tvDateStampic = root.findViewById<TextView>(R.id.tv_date_stampic)
            val currentDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
            tvDateStampic?.apply {
                text = "$currentDate • Stampic"
                typeface = pretendardMedium
                // 디자인 가이드 15 (375 기준) → 기기에 맞춰 가변
                setTextSize(TypedValue.COMPLEX_UNIT_SP, DesignUtils.getScaledTextSize(this@TemplateView.context, 15f))
                // 행간 100%
                setLineSpacing(0f, 1.0f)
                // 자간 -2%는 이미 XML에 letterSpacing="-0.02"로 설정됨

                // 텍스트 그림자: x=0, y=0, 흐림=5px, #000000 45%
                setShadowLayer(
                    DesignUtils.dpToPx(this@TemplateView.context, 5f),  // 블러 반경 5px
                    0f,  // x 오프셋 0
                    0f,  // y 오프셋 0
                    0x73000000.toInt()  // #000000 45%
                )
            }

            // === 로고 표시/숨김 및 크기 조정 ===
            val ivLogo = root.findViewById<ImageView>(R.id.iv_logo)
            ivLogo?.apply {
                visibility = if (showLogo) View.VISIBLE else View.GONE

                // 로고 크기를 기기에 맞춰 조정 (디자인 가이드에 따라 적절한 크기 설정)
                // 예: 48px 기준으로 가변
                layoutParams = layoutParams?.apply {
                    val logoSize = DesignUtils.dpToPxInt(this@TemplateView.context, 48f)
                    width = logoSize
                    height = logoSize
                }
            }

            // === 여백 조정 (375px 기준) ===
            adjustBasic1Margins(root)
        }
    }

    /**
     * Basic 1 템플릿 여백 조정 (375px 기준 가변)
     */
    private fun adjustBasic1Margins(root: View) {
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val tvDateStampic = root.findViewById<TextView>(R.id.tv_date_stampic)
        val ivLogo = root.findViewById<ImageView>(R.id.iv_logo)

        // 시간: 상단 여백 24px
        (tvTime?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            topMargin = DesignUtils.dpToPxInt(context, 24f)
            tvTime.layoutParams = this
        }

        // 날짜+Stampic: 시간 아래 8px
        (tvDateStampic?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            topMargin = DesignUtils.dpToPxInt(context, 8f)
            tvDateStampic.layoutParams = this
        }

        // 로고: 오른쪽 16px, 아래 16px
        (ivLogo?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            bottomMargin = DesignUtils.dpToPxInt(context, 16f)
            marginEnd = DesignUtils.dpToPxInt(context, 16f)
            ivLogo.layoutParams = this
        }
    }

    /**
     * Basic 2 템플릿 데이터 바인딩
     */
    private fun bindBasic2Template(showLogo: Boolean) {
        templateRootView?.let { root ->
            // SUIT Heavy 폰트 로드
            val suitHeavy = try {
                ResourcesCompat.getFont(context, R.font.suit_heavy)
            } catch (e: Exception) {
                null
            }

            // === 날짜 설정 (YYYY년 MM월 DD일 (요일)) ===
            val tvDate = root.findViewById<TextView>(R.id.tv_date)
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // 요일 구하기 (한글)
            val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "일"
                Calendar.MONDAY -> "월"
                Calendar.TUESDAY -> "화"
                Calendar.WEDNESDAY -> "수"
                Calendar.THURSDAY -> "목"
                Calendar.FRIDAY -> "금"
                Calendar.SATURDAY -> "토"
                else -> ""
            }

            val dateText = "${year}년 ${month}월 ${day}일 (${dayOfWeek})"

            tvDate?.apply {
                text = dateText
                suitHeavy?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, DesignUtils.getScaledTextSize(this@TemplateView.context, 24f))
                setShadowLayer(
                    DesignUtils.dpToPx(this@TemplateView.context, 5f),
                    0f,
                    0f,
                    0x73000000.toInt()
                )
            }

            // === 시간 설정 (오전/오후 HH:mm) ===
            val tvTime = root.findViewById<TextView>(R.id.tv_time)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val amPm = if (hour < 12) "오전" else "오후"
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val timeText = String.format("%s %02d:%02d", amPm, displayHour, minute)

            tvTime?.apply {
                text = timeText
                suitHeavy?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, DesignUtils.getScaledTextSize(this@TemplateView.context, 24f))
                setShadowLayer(
                    DesignUtils.dpToPx(this@TemplateView.context, 5f),
                    0f,
                    0f,
                    0x73000000.toInt()
                )
            }

            // === Stampic 아이콘 ===
            val ivLogoIcon = root.findViewById<ImageView>(R.id.iv_logo_icon)
            ivLogoIcon?.apply {
                visibility = if (showLogo) View.VISIBLE else View.GONE

                // 아이콘 크기를 기기에 맞춰 조정 (38dp 기준)
                layoutParams = layoutParams?.apply {
                    val iconSize = DesignUtils.dpToPxInt(this@TemplateView.context, 38f)
                    width = iconSize
                    height = iconSize
                }
            }

            // === 여백 조정 ===
            adjustBasic2Margins(root)
        }
    }

    /**
     * Basic 2 템플릿 여백 조정
     */
    private fun adjustBasic2Margins(root: View) {
        val ivLogoIcon = root.findViewById<ImageView>(R.id.iv_logo_icon)
        val datetimeContainer = root.findViewById<LinearLayout>(R.id.datetime_container)

        // 아이콘: 상단 16px, 우측 16px
        (ivLogoIcon?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            topMargin = DesignUtils.dpToPxInt(context, 16f)
            marginEnd = DesignUtils.dpToPxInt(context, 16f)
            ivLogoIcon.layoutParams = this
        }

        // 날짜/시간 컨테이너: 좌측 24px, 하단 24px
        (datetimeContainer?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            marginStart = DesignUtils.dpToPxInt(context, 24f)
            bottomMargin = DesignUtils.dpToPxInt(context, 24f)
            datetimeContainer.layoutParams = this
        }
    }

    /**
     * Moody 1 템플릿 데이터 바인딩
     */
    private fun bindMoody1Template(showLogo: Boolean) {
        templateRootView?.let { root ->
            // movesans 폰트 로드
            val movesansFont = try {
                ResourcesCompat.getFont(context, R.font.movesans)
            } catch (e: Exception) {
                null  // 폰트 로드 실패 시 null (시스템 기본 폰트 사용)
            }

            // === 날짜 설정 (E, d MMM) ===
            val tvDate = root.findViewById<TextView>(R.id.tv_date)
            val dateFormat = SimpleDateFormat("E, d MMM", Locale.US)
            val currentDate = dateFormat.format(Date()).uppercase(Locale.US)
            tvDate?.apply {
                text = currentDate
                movesansFont?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, DesignUtils.getScaledTextSize(this@TemplateView.context, 30f))
                setLineSpacing(0f, 1.0f)
                setShadowLayer(
                    DesignUtils.dpToPx(this@TemplateView.context, 5f),
                    0f,
                    0f,
                    0x73000000.toInt()
                )
            }

            // === 시간 설정 (a hh:mm, Locale: US) ===
            val tvTime = root.findViewById<TextView>(R.id.tv_time)
            val timeFormat = SimpleDateFormat("a hh:mm", Locale.US)
            val currentTime = timeFormat.format(Date()).uppercase(Locale.US)
            tvTime?.apply {
                text = currentTime
                movesansFont?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, DesignUtils.getScaledTextSize(this@TemplateView.context, 16f))
                setLineSpacing(0f, 1.0f)
                setShadowLayer(
                    DesignUtils.dpToPx(this@TemplateView.context, 5f),
                    0f,
                    0f,
                    0x73000000.toInt()
                )
            }

            // === Stampic 로고 이미지 ===
            val ivStampicLogo = root.findViewById<ImageView>(R.id.iv_stampic_logo)
            ivStampicLogo?.visibility = if (showLogo) View.VISIBLE else View.GONE

            // === 여백 조정 ===
            adjustMoody1Margins(root)
        }
    }

    /**
     * Moody 1 템플릿 여백 조정
     */
    private fun adjustMoody1Margins(root: View) {
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        val tvTime = root.findViewById<TextView>(R.id.tv_time)
        val ivStampicLogo = root.findViewById<ImageView>(R.id.iv_stampic_logo)

        (tvDate?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            topMargin = DesignUtils.dpToPxInt(context, 24f)
            tvDate.layoutParams = this
        }

        (tvTime?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            topMargin = DesignUtils.dpToPxInt(context, 4f)
            tvTime.layoutParams = this
        }

        (ivStampicLogo?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            bottomMargin = DesignUtils.dpToPxInt(context, 16f)
            ivStampicLogo.layoutParams = this
        }
    }

    /**
     * Moody 2 템플릿 데이터 바인딩
     */
    private fun bindMoody2Template(showLogo: Boolean) {
        templateRootView?.let { root ->
            // Suite ExtraLight 폰트 로드
            val suiteExtraLight = try {
                ResourcesCompat.getFont(context, R.font.suit_extralight)
            } catch (e: Exception) {
                null
            }

            // Suite Bold 폰트 로드
            val suiteBold = try {
                ResourcesCompat.getFont(context, R.font.suit_bold)
            } catch (e: Exception) {
                null
            }

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTime = timeFormat.format(Date())
            val timeParts = currentTime.split(":")
            val hour = timeParts[0]
            val minute = timeParts[1]

            // 375px 기준 가변 크기 계산
            val scaledTimeSize = DesignUtils.getScaledTextSize(context, 100f)
            val scaledDateSize = DesignUtils.getScaledTextSize(context, 20f)

            // 그림자: x0 y0 blur10 #000000 30%
            val shadowRadius = DesignUtils.dpToPx(context, 10f)
            val shadowColor = 0x4D000000.toInt() // 30%

            // 시간 첫째 자리
            root.findViewById<TextView>(R.id.tv_hour_1)?.apply {
                text = hour[0].toString()
                suiteExtraLight?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledTimeSize)
                setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
            }

            // 시간 둘째 자리
            root.findViewById<TextView>(R.id.tv_hour_2)?.apply {
                text = hour[1].toString()
                suiteExtraLight?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledTimeSize)
                setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
            }

            // 분 첫째 자리
            root.findViewById<TextView>(R.id.tv_minute_1)?.apply {
                text = minute[0].toString()
                suiteExtraLight?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledTimeSize)
                setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
            }

            // 분 둘째 자리
            root.findViewById<TextView>(R.id.tv_minute_2)?.apply {
                text = minute[1].toString()
                suiteExtraLight?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledTimeSize)
                setShadowLayer(shadowRadius, 0f, 0f, shadowColor)
            }

            // 날짜 (YYYY.MM.DD)
            root.findViewById<TextView>(R.id.tv_date)?.apply {
                val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                text = dateFormat.format(Date())
                suiteBold?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledDateSize)
            }

            // 로고
            root.findViewById<ImageView>(R.id.iv_stampic_logo)?.visibility =
                if (showLogo) View.VISIBLE else View.GONE

            // 여백 조정
            adjustMoody2Margins(root)
        }
    }

    /**
     * Moody 2 템플릿 여백 조정
     */
    private fun adjustMoody2Margins(root: View) {
        val ivStampicLogo = root.findViewById<ImageView>(R.id.iv_stampic_logo)
        val ivCircleBg = root.findViewById<ImageView>(R.id.iv_circle_bg)
        val tvDate = root.findViewById<TextView>(R.id.tv_date)

        // 로고: 상단 여백 16px
        (ivStampicLogo?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            topMargin = DesignUtils.dpToPxInt(context, 16f)
            ivStampicLogo.layoutParams = this
        }

        // 원형 배경: 220dp 크기를 기기에 맞춰 조정
        (ivCircleBg?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            val circleSize = DesignUtils.dpToPxInt(context, 220f)
            width = circleSize
            height = circleSize
            ivCircleBg.layoutParams = this
        }

        // 날짜: 하단 여백 16px
        (tvDate?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            bottomMargin = DesignUtils.dpToPxInt(context, 16f)
            tvDate.layoutParams = this
        }

        // 숫자 간격 6px (Space로 이미 설정됨)
    }

    /**
     * Active 1 템플릿 데이터 바인딩
     */
    private fun bindActive1Template(showLogo: Boolean) {
        templateRootView?.let { root ->
            // 기후위기 폰트 로드
            val gihugwigiFont = try {
                ResourcesCompat.getFont(context, R.font.gihugwigi1990)
            } catch (e: Exception) {
                null  // 폰트 로드 실패 시 null (시스템 기본 폰트 사용)
            }

            // === 시간 설정 (HH:mm) ===
            val tvTime = root.findViewById<TextView>(R.id.tv_time)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTime = timeFormat.format(Date())
            tvTime?.apply {
                text = currentTime
                // 폰트 적용
                gihugwigiFont?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, DesignUtils.getScaledTextSize(this@TemplateView.context, 50f))
                setShadowLayer(
                    DesignUtils.dpToPx(this@TemplateView.context, 5f),
                    0f,
                    0f,
                    0x73000000.toInt()
                )
            }

            // === 날짜 설정 (E, d MMM) ===
            val tvDate = root.findViewById<TextView>(R.id.tv_date)
            val dateFormat = SimpleDateFormat("E, d MMM", Locale.US)
            val currentDate = dateFormat.format(Date()).uppercase(Locale.US)
            tvDate?.apply {
                text = currentDate
                // 폰트 적용
                gihugwigiFont?.let { typeface = it }
                // 폰트: 기후위기 16
                setTextSize(TypedValue.COMPLEX_UNIT_SP, DesignUtils.getScaledTextSize(this@TemplateView.context, 16f))
                // 그림자: x=0, y=0, 흐림=5px, #000000 45%
                setShadowLayer(
                    DesignUtils.dpToPx(this@TemplateView.context, 5f),
                    0f,
                    0f,
                    0x73000000.toInt()
                )
            }

            // === Stampic 로고 이미지 ===
            val ivStampicLogo = root.findViewById<ImageView>(R.id.iv_stampic_logo)
            ivStampicLogo?.visibility = if (showLogo) View.VISIBLE else View.GONE

            // === 여백 조정 ===
            adjustActive1Margins(root)
        }
    }

    /**
     * Active 1 템플릿 여백 조정
     */
    private fun adjustActive1Margins(root: View) {
        val ivStampicLogo = root.findViewById<ImageView>(R.id.iv_stampic_logo)

        // 로고: 아래 16px
        (ivStampicLogo?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            bottomMargin = DesignUtils.dpToPxInt(context, 16f)
            ivStampicLogo.layoutParams = this
        }
    }

    /**
     * Digital 1 템플릿 데이터 바인딩
     */
    private fun bindDigital1Template(showLogo: Boolean) {
        templateRootView?.let { root ->
            val suitExtraBold = try {
                ResourcesCompat.getFont(context, R.font.suit_extrabold)
            } catch (e: Exception) {
                null
            }

            val suitBold = try {
                ResourcesCompat.getFont(context, R.font.suit_bold)
            } catch (e: Exception) {
                null
            }

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val currentTime = timeFormat.format(Date())
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
                setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
                setShadowLayer(dropShadowRadius, dropShadowDx, dropShadowDy, dropShadowColor)
            }

            // 시간 둘째 자리
            root.findViewById<TextView>(R.id.tv_hour_2)?.apply {
                text = hour[1].toString()
                suitExtraBold?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
                setShadowLayer(dropShadowRadius, dropShadowDx, dropShadowDy, dropShadowColor)
            }

            // 콜론
            root.findViewById<TextView>(R.id.tv_colon)?.apply {
                suitExtraBold?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
            }

            // 분 첫째 자리
            root.findViewById<TextView>(R.id.tv_minute_1)?.apply {
                text = minute[0].toString()
                suitExtraBold?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
                setShadowLayer(dropShadowRadius, dropShadowDx, dropShadowDy, dropShadowColor)
            }

            // 분 둘째 자리
            root.findViewById<TextView>(R.id.tv_minute_2)?.apply {
                text = minute[1].toString()
                suitExtraBold?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledTextSize)
                setShadowLayer(dropShadowRadius, dropShadowDx, dropShadowDy, dropShadowColor)
            }

            // 날짜
            root.findViewById<TextView>(R.id.tv_date)?.apply {
                val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                text = dateFormat.format(Date())
                suitBold?.let { typeface = it }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledDateSize)
            }

            // 로고
            root.findViewById<ImageView>(R.id.iv_stampic_logo)?.apply {
                visibility = if (showLogo) View.VISIBLE else View.GONE
                // 로고는 원본 크기 유지 (wrap_content)
            }

            // 여백 조정 (375px 기준 가변)
            adjustDigital1Margins(root)
        }
    }

    /**
     * Digital 1 템플릿 여백 조정 (375px 기준 가변)
     */
    private fun adjustDigital1Margins(root: View) {
        // 375px 기준 가변 크기 계산
        val boxWidth = DesignUtils.dpToPxInt(context, 45f)
        val boxHeight = DesignUtils.dpToPxInt(context, 60f)
        val gap = DesignUtils.dpToPxInt(context, 6f)

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
        val tvDate = root.findViewById<TextView>(R.id.tv_date)
        (tvDate?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            bottomMargin = DesignUtils.dpToPxInt(context, 16f)
            tvDate.layoutParams = this
        }

        // 로고 - 상단 여백 16px (가변) - 이미 XML에서 설정되어 있지만 확실하게
        val ivStampicLogo = root.findViewById<ImageView>(R.id.iv_stampic_logo)
        (ivStampicLogo?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            topMargin = DesignUtils.dpToPxInt(context, 16f)
            ivStampicLogo.layoutParams = this
        }
    }

    /**
     * 로고 표시 상태 변경
     */
    fun setLogoVisibility(visible: Boolean) {
        templateRootView?.let { root ->
            // Basic 1 템플릿의 ImageView 로고
            root.findViewById<ImageView>(R.id.iv_logo)?.visibility =
                if (visible) View.VISIBLE else View.GONE
            // Basic 2 템플릿의 아이콘
            root.findViewById<ImageView>(R.id.iv_logo_icon)?.visibility =
                if (visible) View.VISIBLE else View.GONE
            // Moody, Active, Digital 템플릿의 ImageView 로고
            root.findViewById<ImageView>(R.id.iv_stampic_logo)?.visibility =
                if (visible) View.VISIBLE else View.GONE
        }
    }

    /**
     * 현재 템플릿 정보
     */
    fun getCurrentTemplate(): Template? = currentTemplate
}