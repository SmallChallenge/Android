package com.project.stampy.template

import android.content.Context
import android.util.TypedValue

/**
 * 디자인 가이드: 375px 기준
 * 실제 기기에서는 (기기 너비 / 375) 비율로 가변 적용
 */
object DesignUtils {

    private const val DESIGN_WIDTH = 375f

    /**
     * 디자인 px을 기기에 맞는 실제 px로 변환
     * @param context Context
     * @param designPx 디자인 가이드의 px 값
     * @return 기기에 맞게 조정된 px 값
     */
    fun dpToPx(context: Context, designPx: Float): Float {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val ratio = screenWidth / DESIGN_WIDTH
        return designPx * ratio
    }

    /**
     * 디자인 px을 기기에 맞는 실제 px로 변환 (Int)
     */
    fun dpToPxInt(context: Context, designPx: Float): Int {
        return dpToPx(context, designPx).toInt()
    }

    /**
     * 디자인 가이드의 텍스트 크기(px)를 기기에 맞는 sp로 변환
     * 디자인의 "px"는 실제로 dp를 의미하므로 375dp 기준으로 가변 계산
     * @param context Context
     * @param designTextSize 디자인 가이드의 텍스트 크기 (예: 55)
     * @return 기기에 맞게 조정된 sp 값
     */
    fun getScaledTextSize(context: Context, designTextSize: Float): Float {
        // 화면 너비를 dp로 변환
        val screenWidthDp = context.resources.displayMetrics.widthPixels /
                context.resources.displayMetrics.density
        // (기기dp너비 / 375) * 디자인텍스트크기
        return (screenWidthDp / DESIGN_WIDTH) * designTextSize
    }
}