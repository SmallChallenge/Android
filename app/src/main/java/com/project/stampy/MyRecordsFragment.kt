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
    private lateinit var categoryStudy: LinearLayout
    private lateinit var categoryExercise: LinearLayout
    private lateinit var categoryFood: LinearLayout
    private lateinit var categoryEtc: LinearLayout

    private lateinit var ivCategoryAll: ImageView
    private lateinit var ivCategoryStudy: ImageView
    private lateinit var ivCategoryExercise: ImageView
    private lateinit var ivCategoryFood: ImageView
    private lateinit var ivCategoryEtc: ImageView

    private lateinit var tvCategoryAll: TextView
    private lateinit var tvCategoryStudy: TextView
    private lateinit var tvCategoryExercise: TextView
    private lateinit var tvCategoryFood: TextView
    private lateinit var tvCategoryEtc: TextView

    private lateinit var rvPhotos: RecyclerView
    private lateinit var tvEmptyState: TextView

    private lateinit var photoAdapter: PhotoGridAdapter
    private var selectedCategory = "전체"

    // 카테고리 뷰 맵
    private val categoryViews by lazy {
        mapOf(
            "전체" to Triple(categoryAll, ivCategoryAll, tvCategoryAll),
            "공부" to Triple(categoryStudy, ivCategoryStudy, tvCategoryStudy),
            "운동" to Triple(categoryExercise, ivCategoryExercise, tvCategoryExercise),
            "음식" to Triple(categoryFood, ivCategoryFood, tvCategoryFood),
            "기타" to Triple(categoryEtc, ivCategoryEtc, tvCategoryEtc)
        )
    }

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
        initViews(view)

        // RecyclerView 설정
        setupRecyclerView()

        // 카테고리 클릭 리스너
        setupCategoryListeners()

        // 사진 로드
        loadPhotos()
    }

    private fun initViews(view: View) {
        categoryAll = view.findViewById(R.id.category_all)
        categoryStudy = view.findViewById(R.id.category_study)
        categoryExercise = view.findViewById(R.id.category_exercise)
        categoryFood = view.findViewById(R.id.category_food)
        categoryEtc = view.findViewById(R.id.category_etc)

        ivCategoryAll = view.findViewById(R.id.iv_category_all)
        ivCategoryStudy = view.findViewById(R.id.iv_category_study)
        ivCategoryExercise = view.findViewById(R.id.iv_category_exercise)
        ivCategoryFood = view.findViewById(R.id.iv_category_food)
        ivCategoryEtc = view.findViewById(R.id.iv_category_etc)

        tvCategoryAll = view.findViewById(R.id.tv_category_all)
        tvCategoryStudy = view.findViewById(R.id.tv_category_study)
        tvCategoryExercise = view.findViewById(R.id.tv_category_exercise)
        tvCategoryFood = view.findViewById(R.id.tv_category_food)
        tvCategoryEtc = view.findViewById(R.id.tv_category_etc)

        rvPhotos = view.findViewById(R.id.rv_photos)
        tvEmptyState = view.findViewById(R.id.tv_empty_state)
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
        categoryViews.forEach { (category, views) ->
            views.first.setOnClickListener {
                selectCategory(category)
            }
        }
    }

    private fun selectCategory(category: String) {
        selectedCategory = category

        // 모든 카테고리 업데이트
        categoryViews.forEach { (cat, views) ->
            val (_, imageView, textView) = views
            if (cat == category) {
                activateCategory(imageView, textView)
            } else {
                resetCategory(imageView, textView)
            }
        }

        // 사진 필터링
        loadPhotos()
    }

    private fun resetCategory(imageView: ImageView, textView: TextView) {
        imageView.setBackgroundResource(R.drawable.category_circle_unselected)
        imageView.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.gray_600)
        )
        textView.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.gray_600)
        )
    }

    private fun activateCategory(imageView: ImageView, textView: TextView) {
        imageView.setBackgroundResource(R.drawable.category_circle_selected)
        imageView.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.gray_primary)
        )
        textView.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.white)
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
            val photos = photoFiles.map { Photo(it, category = selectedCategory) }
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