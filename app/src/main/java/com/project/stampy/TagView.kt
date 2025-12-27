package com.project.stampy.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.project.stampy.R

/**
 * Tag 컴포넌트
 *
 * 상태:
 * - Active (selected=true): Gray 50, Btn2_B, 배경 Gray 700
 * - Inactive (selected=false): Gray 300, Btn2, 배경 Gray 800 + 선 Gray 700
 * - Pressed: Gray 300, Btn2_B, 배경 Gray 800
 *
 * 사용법:
 * ```xml
 * <com.project.stampy.ui.components.TagView
 *     android:id="@+id/tag_public"
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     android:text="전체 공개" />
 * ```
 */
class TagView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        // 기본 설정 - 높이만 고정
        minimumHeight = dpToPx(37)

        // 패딩 설정 (좌우 12dp, 상하 10dp)
        setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))

        // 배경 selector
        setBackgroundResource(R.drawable.bg_tag_selector)

        // 텍스트 색상 selector
        setTextColor(ContextCompat.getColorStateList(context, R.color.tag_text_color_selector))

        // 기본 텍스트 스타일 (Inactive)
        setTextAppearance(R.style.TextAppearance_App_Button2_Medium)

        // 중앙 정렬
        gravity = android.view.Gravity.CENTER

        // 클릭 가능
        isClickable = true
        isFocusable = true

        // 최소 크기 제거
        minWidth = 0
        minHeight = 0

        // 상태 변경 리스너 (Active/Inactive에 따라 폰트 변경)
        setOnClickListener {
            isSelected = !isSelected
            updateTextAppearance()
        }
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        updateTextAppearance()
    }

    /**
     * 상태에 따라 텍스트 스타일 변경
     */
    private fun updateTextAppearance() {
        if (isSelected) {
            // Active: Btn2_B
            setTextAppearance(R.style.TextAppearance_App_Button2_Large)
        } else {
            // Inactive: Btn2
            setTextAppearance(R.style.TextAppearance_App_Button2_Medium)
        }
    }

    /**
     * dp를 px로 변환
     */
    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}