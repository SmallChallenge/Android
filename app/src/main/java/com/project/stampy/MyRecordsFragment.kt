package com.project.stampy

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.stampy.data.model.Photo
import java.io.File

class MyRecordsFragment : Fragment() {

    // 카테고리 뷰들
    private lateinit var categoryAll: LinearLayout
    private lateinit var categoryExercise: LinearLayout
    private lateinit var categoryFood: LinearLayout
    private lateinit var categoryStudy: LinearLayout

    private lateinit var ivCategoryAll: ImageView
    private lateinit var ivCategoryExercise: ImageView
    private lateinit var ivCategoryFood: ImageView
    private lateinit var ivCategoryStudy: ImageView

    private lateinit var tvCategoryAll: TextView
    private lateinit var tvCategoryExercise: TextView
    private lateinit var tvCategoryFood: TextView
    private lateinit var tvCategoryStudy: TextView

    private lateinit var rvPhotos: RecyclerView
    private lateinit var tvEmptyState: TextView

    private lateinit var photoAdapter: PhotoGridAdapter
    private var selectedCategory = "전체"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_records, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // View 초기화
        categoryAll = view.findViewById(R.id.category_all)
        categoryExercise = view.findViewById(R.id.category_exercise)
        categoryFood = view.findViewById(R.id.category_food)
        categoryStudy = view.findViewById(R.id.category_study)

        ivCategoryAll = view.findViewById(R.id.iv_category_all)
        ivCategoryExercise = view.findViewById(R.id.iv_category_exercise)
        ivCategoryFood = view.findViewById(R.id.iv_category_food)
        ivCategoryStudy = view.findViewById(R.id.iv_category_study)

        tvCategoryAll = view.findViewById(R.id.tv_category_all)
        tvCategoryExercise = view.findViewById(R.id.tv_category_exercise)
        tvCategoryFood = view.findViewById(R.id.tv_category_food)
        tvCategoryStudy = view.findViewById(R.id.tv_category_study)

        rvPhotos = view.findViewById(R.id.rv_photos)
        tvEmptyState = view.findViewById(R.id.tv_empty_state)

        // RecyclerView 설정
        setupRecyclerView()

        // 카테고리 클릭 리스너
        setupCategoryListeners()

        // 사진 로드
        loadPhotos()
    }

    private fun setupRecyclerView() {
        photoAdapter = PhotoGridAdapter()

        // 3열 그리드 레이아웃
        rvPhotos.layoutManager = GridLayoutManager(requireContext(), 3)
        rvPhotos.adapter = photoAdapter

        // 사진 클릭 리스너
        photoAdapter.setOnPhotoClickListener { photo ->
            // TODO: 사진 상세보기 화면으로 이동
            Toast.makeText(requireContext(), "사진 클릭: ${photo.file.name}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCategoryListeners() {
        categoryAll.setOnClickListener {
            selectCategory("전체")
        }

        categoryExercise.setOnClickListener {
            selectCategory("운동")
        }

        categoryFood.setOnClickListener {
            selectCategory("음식")
        }

        categoryStudy.setOnClickListener {
            selectCategory("공부")
        }
    }

    private fun selectCategory(category: String) {
        selectedCategory = category

        // 모든 카테고리 초기화 (선택 안됨 상태)
        resetCategory(ivCategoryAll, tvCategoryAll)
        resetCategory(ivCategoryExercise, tvCategoryExercise)
        resetCategory(ivCategoryFood, tvCategoryFood)
        resetCategory(ivCategoryStudy, tvCategoryStudy)

        // 선택된 카테고리 활성화
        when (category) {
            "전체" -> activateCategory(ivCategoryAll, tvCategoryAll)
            "운동" -> activateCategory(ivCategoryExercise, tvCategoryExercise)
            "음식" -> activateCategory(ivCategoryFood, tvCategoryFood)
            "공부" -> activateCategory(ivCategoryStudy, tvCategoryStudy)
        }

        // 사진 필터링 (현재는 전체만 표시)
        loadPhotos()
    }

    private fun resetCategory(imageView: ImageView, textView: TextView) {
        imageView.setBackgroundResource(R.drawable.category_circle_unselected)
        imageView.setColorFilter(
            ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        )
        textView.setTextColor(
            ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
        )
    }

    private fun activateCategory(imageView: ImageView, textView: TextView) {
        imageView.setBackgroundResource(R.drawable.category_circle_selected)
        imageView.setColorFilter(
            ContextCompat.getColor(requireContext(), android.R.color.black)
        )
        textView.setTextColor(
            ContextCompat.getColor(requireContext(), android.R.color.white)
        )
    }

    /**
     * 사진 로드
     */
    fun loadPhotos() {
        val picturesDir = File(requireContext().filesDir, "Pictures")

        if (!picturesDir.exists() || !picturesDir.isDirectory) {
            showEmptyState()
            return
        }

        val photoFiles = picturesDir.listFiles { file ->
            file.isFile && file.name.startsWith("STAMPY_") && file.extension == "jpg"
        }?.sortedByDescending { it.lastModified() } ?: emptyList()

        if (photoFiles.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
            val photos = photoFiles.map { Photo(it) }
            photoAdapter.setPhotos(photos)

            Log.d("MyRecordsFragment", "사진 ${photos.size}개 로드됨")
        }
    }

    /**
     * 사진 목록 새로고침 (MainActivity에서 호출)
     */
    fun refreshPhotos() {
        loadPhotos()
    }

    private fun showEmptyState() {
        rvPhotos.visibility = View.GONE
        tvEmptyState.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        rvPhotos.visibility = View.VISIBLE
        tvEmptyState.visibility = View.GONE
    }
}