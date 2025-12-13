package com.project.stampy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyRecordsFragment : Fragment() {

    private lateinit var photoGrid: RecyclerView
    private lateinit var categoryAll: LinearLayout
    private lateinit var categoryExercise: LinearLayout
    private lateinit var categoryFood: LinearLayout
    private lateinit var categoryStudy: LinearLayout

    private var selectedCategory: String = "전체"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_records, container, false)

        photoGrid = view.findViewById(R.id.photo_grid)
        photoGrid.layoutManager = GridLayoutManager(context, 3)

        // 카테고리 뷰 초기화
        categoryAll = view.findViewById(R.id.category_all)
        categoryExercise = view.findViewById(R.id.category_exercise)
        categoryFood = view.findViewById(R.id.category_food)
        categoryStudy = view.findViewById(R.id.category_study)

        // 카테고리 클릭 리스너 설정
        categoryAll.setOnClickListener { selectCategory("전체", categoryAll) }
        categoryExercise.setOnClickListener { selectCategory("운동", categoryExercise) }
        categoryFood.setOnClickListener { selectCategory("음식", categoryFood) }
        categoryStudy.setOnClickListener { selectCategory("공부", categoryStudy) }

        return view
    }

    private fun selectCategory(category: String, selectedView: LinearLayout) {
        selectedCategory = category

        // 모든 카테고리를 비선택 상태로
        resetCategoryStyle(categoryAll)
        resetCategoryStyle(categoryExercise)
        resetCategoryStyle(categoryFood)
        resetCategoryStyle(categoryStudy)

        // 선택된 카테고리 스타일 변경
        val imageView = selectedView.getChildAt(0) as ImageView
        val textView = selectedView.getChildAt(1) as TextView

        imageView.setBackgroundResource(R.drawable.category_circle_selected)
        imageView.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.black))
        textView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
    }

    private fun resetCategoryStyle(categoryView: LinearLayout) {
        val imageView = categoryView.getChildAt(0) as ImageView
        val textView = categoryView.getChildAt(1) as TextView

        imageView.setBackgroundResource(R.drawable.category_circle_unselected)
        imageView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray_600))
        textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_600))
    }
}