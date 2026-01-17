package com.project.stampy.template

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.project.stampy.R

/**
 * 템플릿 뷰 클래스
 * 선택된 템플릿을 동적으로 inflate하고 데이터 바인딩
 */
class TemplateView(context: Context) : FrameLayout(context) {

    private var currentTemplate: Template? = null
    private var templateRootView: View? = null
    private var currentShowLogo: Boolean = true
    private var photoTakenAtTimestamp: Long = System.currentTimeMillis()

    /**
     * 사진 촬영 시간 설정
     */
    fun setPhotoTakenAt(timestamp: Long) {
        photoTakenAtTimestamp = timestamp
        // 템플릿이 이미 적용되어 있으면 시간 업데이트
        currentTemplate?.let { template ->
            applyTemplate(template, currentShowLogo)
        }
    }

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
        currentShowLogo = showLogo

        // 카테고리별 바인더 선택 및 실행
        templateRootView?.let { root ->
            val binder = when (template.category) {
                TemplateCategory.BASIC -> BasicTemplateBinder(context, root)
                TemplateCategory.MOODY -> MoodyTemplateBinder(context, root)
                TemplateCategory.ACTIVE -> ActiveTemplateBinder(context, root)
                TemplateCategory.DIGITAL -> DigitalTemplateBinder(context, root)
            }
            binder.bind(template, showLogo, photoTakenAtTimestamp)
        }
    }

    /**
     * 사진 설정 (모든 템플릿에서 사용 가능)
     * @param bitmap 설정할 사진 비트맵
     */
    fun setPhoto(bitmap: Bitmap?) {
        templateRootView?.let { root ->
            // Moody 3 템플릿의 경우
            val ivPhoto = root.findViewById<ImageView>(R.id.iv_photo)
            ivPhoto?.setImageBitmap(bitmap)

            // 다른 템플릿에서도 iv_photo를 사용한다면 동일하게 작동
        }
    }

    /**
     * 로고 표시 상태 변경
     */
    fun setLogoVisibility(visible: Boolean) {
        currentShowLogo = visible
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