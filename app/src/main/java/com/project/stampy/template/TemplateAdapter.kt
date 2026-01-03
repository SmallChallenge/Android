package com.project.stampy.template

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.stampy.R

/**
 * 템플릿 RecyclerView 어댑터
 */
class TemplateAdapter(
    private val onTemplateClick: (Template) -> Unit
) : ListAdapter<Template, TemplateAdapter.TemplateViewHolder>(TemplateDiffCallback()) {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_template, parent, false)
        return TemplateViewHolder(view, onTemplateClick)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        val template = getItem(position)
        holder.bind(template, position == selectedPosition)
    }

    /**
     * 템플릿 선택 상태 업데이트
     */
    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }

    inner class TemplateViewHolder(
        itemView: View,
        private val onTemplateClick: (Template) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val container: ConstraintLayout = itemView.findViewById(R.id.template_container)
        private val templatePreview: ImageView = itemView.findViewById(R.id.iv_template_preview)
        private val templateName: TextView = itemView.findViewById(R.id.tv_template_name)

        fun bind(template: Template, isSelected: Boolean) {
            // 템플릿 이름 설정
            templateName.text = template.name

            // 선택 상태에 따른 배경 변경
            if (isSelected) {
                container.setBackgroundResource(R.drawable.bg_template_item_selected)
            } else {
                container.setBackgroundResource(R.drawable.bg_template_item)
            }

            // 템플릿 프리뷰 이미지 설정 (TODO: 실제 프리뷰 이미지)
            // templatePreview.setImageResource(template.previewImageResId)

            // 크기 조정 (375px 기준)
            val size = DesignUtils.dpToPxInt(itemView.context, 90f)
            container.layoutParams = container.layoutParams.apply {
                width = size
                height = size
            }

            // 클릭 이벤트
            itemView.setOnClickListener {
                setSelectedPosition(bindingAdapterPosition)
                onTemplateClick(template)
            }
        }
    }

    private class TemplateDiffCallback : DiffUtil.ItemCallback<Template>() {
        override fun areItemsTheSame(oldItem: Template, newItem: Template): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Template, newItem: Template): Boolean {
            return oldItem == newItem
        }
    }
}