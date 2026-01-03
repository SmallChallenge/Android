package com.project.stampy.template

import com.project.stampy.R

/**
 * 템플릿 관리 클래스
 */
object TemplateManager {

    private val templates = mutableListOf<Template>()

    init {
        // Basic 카테고리 템플릿
        templates.add(
            Template(
                id = "basic_1",
                category = TemplateCategory.BASIC,
                name = "Basic 1",
                layoutResId = R.layout.template_basic_1
            )
        )

        // Moody 카테고리 템플릿
        templates.add(
            Template(
                id = "moody_1",
                category = TemplateCategory.MOODY,
                name = "Moody 1",
                layoutResId = R.layout.template_moody_1
            )
        )

        // TODO: 나머지 템플릿 추가
        // templates.add(Template("basic_2", TemplateCategory.BASIC, "Basic 2", R.layout.template_basic_2))
        // templates.add(Template("active_1", TemplateCategory.ACTIVE, "Active 1", R.layout.template_active_1))
        // ...
    }

    /**
     * 카테고리별 템플릿 목록 조회
     */
    fun getTemplatesByCategory(category: TemplateCategory): List<Template> {
        return templates.filter { it.category == category }
    }

    /**
     * 템플릿 ID로 조회
     */
    fun getTemplateById(id: String): Template? {
        return templates.find { it.id == id }
    }

    /**
     * 모든 템플릿 조회
     */
    fun getAllTemplates(): List<Template> {
        return templates.toList()
    }
}