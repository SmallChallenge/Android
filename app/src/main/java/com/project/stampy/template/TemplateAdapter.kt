package com.project.stampy.template

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
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

        private val container: View = itemView.findViewById(R.id.template_card)
        private val card: MaterialCardView = itemView.findViewById(R.id.template_card)
        private val templatePreview: ImageView = itemView.findViewById(R.id.iv_template_preview)

        fun bind(template: Template, isSelected: Boolean) {
            // 썸네일 이미지 설정
            templatePreview.setImageResource(template.thumbnailResId)
            templatePreview.visibility = View.VISIBLE

            // 선택 stroke
            val ctx = itemView.context
            if (isSelected) {
                card.strokeWidth = DesignUtils.dpToPxInt(ctx, 2f)
                card.strokeColor = ContextCompat.getColor(ctx, R.color.gray_50)
            } else {
                card.strokeWidth = DesignUtils.dpToPxInt(ctx, 1f)
                card.strokeColor = ContextCompat.getColor(ctx, R.color.gray_700)
            }

            // 크기 조정 (375px 기준)
            val size = DesignUtils.dpToPxInt(ctx, 90f)
            val lp = container.layoutParams
            lp.width = size
            lp.height = size
            container.layoutParams = lp

            // 클릭 이벤트
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    setSelectedPosition(pos)
                    onTemplateClick(template)
                }
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