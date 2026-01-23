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
                layoutResId = R.layout.template_basic_1,
                thumbnailResId = R.drawable.template_basic_1
            )
        )

        templates.add(
            Template(
                id = "basic_2",
                category = TemplateCategory.BASIC,
                name = "Basic 2",
                layoutResId = R.layout.template_basic_2,
                thumbnailResId = R.drawable.template_basic_2
            )
        )

        templates.add(
            Template(
                id = "basic_3",
                category = TemplateCategory.BASIC,
                name = "Basic 3",
                layoutResId = R.layout.template_basic_3,
                thumbnailResId = R.drawable.template_basic_3
            )
        )

        templates.add(
            Template(
                id = "basic_4",
                category = TemplateCategory.BASIC,
                name = "Basic 4",
                layoutResId = R.layout.template_basic_4,
                thumbnailResId = R.drawable.template_basic_4
            )
        )

        templates.add(
            Template(
                id = "basic_5",
                category = TemplateCategory.BASIC,
                name = "Basic 5",
                layoutResId = R.layout.template_basic_5,
                thumbnailResId = R.drawable.template_basic_5
            )
        )

        templates.add(
            Template(
                id = "basic_6",
                category = TemplateCategory.BASIC,
                name = "Basic 6",
                layoutResId = R.layout.template_basic_6,
                thumbnailResId = R.drawable.template_basic_6
            )
        )

        // Moody 카테고리 템플릿
        templates.add(
            Template(
                id = "moody_1",
                category = TemplateCategory.MOODY,
                name = "Moody 1",
                layoutResId = R.layout.template_moody_1,
                thumbnailResId = R.drawable.template_moody_1
            )
        )

        templates.add(
            Template(
                id = "moody_2",
                category = TemplateCategory.MOODY,
                name = "Moody 2",
                layoutResId = R.layout.template_moody_2,
                thumbnailResId = R.drawable.template_moody_2
            )
        )

        templates.add(
            Template(
                id = "moody_3",
                category = TemplateCategory.MOODY,
                name = "Moody 3",
                layoutResId = R.layout.template_moody_3,
                thumbnailResId = R.drawable.template_moody_3
            )
        )

        templates.add(
            Template(
                id = "moody_4",
                category = TemplateCategory.MOODY,
                name = "Moody 4",
                layoutResId = R.layout.template_moody_4,
                thumbnailResId = R.drawable.template_moody_4
            )
        )

        templates.add(
            Template(
                id = "moody_5",
                category = TemplateCategory.MOODY,
                name = "Moody 5",
                layoutResId = R.layout.template_moody_5,
                thumbnailResId = R.drawable.template_moody_5
            )
        )

        templates.add(
            Template(
                id = "moody_6",
                category = TemplateCategory.MOODY,
                name = "Moody 6",
                layoutResId = R.layout.template_moody_6,
                thumbnailResId = R.drawable.template_moody_6
            )
        )

        // Active 카테고리 템플릿
        templates.add(
            Template(
                id = "active_1",
                category = TemplateCategory.ACTIVE,
                name = "Active 1",
                layoutResId = R.layout.template_active_1,
                thumbnailResId = R.drawable.template_active_1
            )
        )

        templates.add(
            Template(
                id = "active_2",
                category = TemplateCategory.ACTIVE,
                name = "Active 2",
                layoutResId = R.layout.template_active_2,
                thumbnailResId = R.drawable.template_active_2
            )
        )

        templates.add(
            Template(
                id = "active_3",
                category = TemplateCategory.ACTIVE,
                name = "Active 3",
                layoutResId = R.layout.template_active_3,
                thumbnailResId = R.drawable.template_active_3
            )
        )

        templates.add(
            Template(
                id = "active_4",
                category = TemplateCategory.ACTIVE,
                name = "active 4",
                layoutResId = R.layout.template_active_4,
                thumbnailResId = R.drawable.template_active_4
            )
        )

        templates.add(
            Template(
                id = "active_5",
                category = TemplateCategory.ACTIVE,
                name = "active 5",
                layoutResId = R.layout.template_active_5,
                thumbnailResId = R.drawable.template_active_5
            )
        )

        templates.add(
            Template(
                id = "active_6",
                category = TemplateCategory.ACTIVE,
                name = "active 6",
                layoutResId = R.layout.template_active_6,
                thumbnailResId = R.drawable.template_active_6
            )
        )

        // Digital 카테고리 템플릿
        templates.add(
            Template(
                id = "digital_1",
                category = TemplateCategory.DIGITAL,
                name = "Digital 1",
                layoutResId = R.layout.template_digital_1,
                thumbnailResId = R.drawable.template_digital_1
            )
        )

        templates.add(
            Template(
                id = "digital_2",
                category = TemplateCategory.DIGITAL,
                name = "Digital 2",
                layoutResId = R.layout.template_digital_2,
                thumbnailResId = R.drawable.template_digital_2
            )
        )

        templates.add(
            Template(
                id = "digital_3",
                category = TemplateCategory.DIGITAL,
                name = "Digital 3",
                layoutResId = R.layout.template_digital_3,
                thumbnailResId = R.drawable.template_digital_3
            )
        )

        templates.add(
            Template(
                id = "digital_4",
                category = TemplateCategory.DIGITAL,
                name = "Digital 4",
                layoutResId = R.layout.template_digital_4,
                thumbnailResId = R.drawable.template_digital_4
            )
        )

        templates.add(
            Template(
                id = "digital_5",
                category = TemplateCategory.DIGITAL,
                name = "Digital 5",
                layoutResId = R.layout.template_digital_5,
                thumbnailResId = R.drawable.template_digital_5
            )
        )
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