package com.project.stampy.template

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
            // TODO: 다른 템플릿 추가
        }
    }

    /**
     * Basic 1 템플릿 데이터 바인딩
     */
    private fun bindBasic1Template(showLogo: Boolean) {
        templateRootView?.let { root ->
            // === 시간 설정 ===
            val tvTime = root.findViewById<TextView>(R.id.tv_time)
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            tvTime?.apply {
                text = currentTime
                // 디자인 가이드 55 (375 기준) → 기기에 맞춰 가변
                setTextSize(TypedValue.COMPLEX_UNIT_SP, DesignUtils.getScaledTextSize(context, 55f))
                // 행간 100%
                setLineSpacing(0f, 1.0f)
                // 자간 -2%는 이미 XML에 letterSpacing="-0.02"로 설정됨

                // 텍스트 그림자: x=0, y=0, 흐림=5px, #000000 45%
                setShadowLayer(
                    DesignUtils.dpToPx(context, 5f),  // 블러 반경 5px
                    0f,  // x 오프셋 0
                    0f,  // y 오프셋 0
                    0x73000000.toInt()  // #000000 45% (0x73 = 115 = 45% of 255)
                )
            }

            // === 날짜 + Stampic 설정 ===
            val tvDateStampic = root.findViewById<TextView>(R.id.tv_date_stampic)
            val currentDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(Date())
            tvDateStampic?.apply {
                text = "$currentDate • Stampic"
                // 디자인 가이드 15 (375 기준) → 기기에 맞춰 가변
                setTextSize(TypedValue.COMPLEX_UNIT_SP, DesignUtils.getScaledTextSize(context, 15f))
                // 행간 100%
                setLineSpacing(0f, 1.0f)
                // 자간 -2%는 이미 XML에 letterSpacing="-0.02"로 설정됨

                // 텍스트 그림자: x=0, y=0, 흐림=5px, #000000 45%
                setShadowLayer(
                    DesignUtils.dpToPx(context, 5f),  // 블러 반경 5px
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
                    val logoSize = DesignUtils.dpToPxInt(context, 48f)
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
        }

        // 날짜+Stampic: 시간 아래 8px
        (tvDateStampic?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            topMargin = DesignUtils.dpToPxInt(context, 8f)
        }

        // 로고: 오른쪽 16px, 아래 16px
        (ivLogo?.layoutParams as? ConstraintLayout.LayoutParams)?.apply {
            bottomMargin = DesignUtils.dpToPxInt(context, 16f)
            marginEnd = DesignUtils.dpToPxInt(context, 16f)
        }

        // 레이아웃 업데이트
        tvTime?.requestLayout()
        tvDateStampic?.requestLayout()
        ivLogo?.requestLayout()
    }

    /**
     * 로고 표시 상태 변경
     */
    fun setLogoVisibility(visible: Boolean) {
        templateRootView?.let { root ->
            val ivLogo = root.findViewById<ImageView>(R.id.iv_logo)
            ivLogo?.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    /**
     * 현재 템플릿 정보
     */
    fun getCurrentTemplate(): Template? = currentTemplate
}