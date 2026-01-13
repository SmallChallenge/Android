package com.project.stampy.template

/**
 * 템플릿 데이터 클래스
 */
data class Template(
    val id: String,                  // 템플릿 고유 ID (예: "basic_1")
    val category: TemplateCategory,  // 카테고리
    val name: String,                // 템플릿 이름 (예: "Basic 1")
    val layoutResId: Int,            // 레이아웃 리소스 ID
    val thumbnailResId: Int          // 썸네일 이미지 리소스 ID
)

/**
 * 템플릿 카테고리
 */
enum class TemplateCategory(val displayName: String) {
    BASIC("Basic"),
    MOODY("Moody"),
    ACTIVE("Active"),
    DIGITAL("Digital")
}