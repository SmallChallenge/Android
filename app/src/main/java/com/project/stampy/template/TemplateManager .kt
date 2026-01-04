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

        templates.add(
            Template(
                id = "basic_2",
                category = TemplateCategory.BASIC,
                name = "Basic 2",
                layoutResId = R.layout.template_basic_2
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

        templates.add(
            Template(
                id = "moody_2",
                category = TemplateCategory.MOODY,
                name = "Moody 2",
                layoutResId = R.layout.template_moody_2
            )
        )

        // Active 카테고리 템플릿
        templates.add(
            Template(
                id = "active_1",
                category = TemplateCategory.ACTIVE,
                name = "Active 1",
                layoutResId = R.layout.template_active_1
            )
        )

        templates.add(
            Template(
                id = "active_2",
                category = TemplateCategory.ACTIVE,
                name = "Active 2",
                layoutResId = R.layout.template_active_2
            )
        )

        // Digital 카테고리 템플릿
        templates.add(
            Template(
                id = "digital_1",
                category = TemplateCategory.DIGITAL,
                name = "Digital 1",
                layoutResId = R.layout.template_digital_1
            )
        )

        templates.add(
            Template(
                id = "digital_2",
                category = TemplateCategory.DIGITAL,
                name = "Digital 2",
                layoutResId = R.layout.template_digital_2
            )
        )

        // TODO: 나머지 템플릿 추가
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