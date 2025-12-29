package com.project.stampy

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.model.Photo
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.ImageRepository
import com.project.stampy.utils.showToast
import kotlinx.coroutines.launch
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
    private lateinit var emptyStateContainer: ConstraintLayout
    private lateinit var btnProfile: ImageView

    private lateinit var photoAdapter: PhotoGridAdapter
    private var selectedCategory = "전체"

    // 서버 API 관련
    private lateinit var tokenManager: TokenManager
    private lateinit var imageRepository: ImageRepository

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

        // 서버 API 초기화
        tokenManager = TokenManager(requireContext())
        RetrofitClient.initialize(tokenManager)
        imageRepository = ImageRepository(tokenManager)

        // View 초기화
        initViews(view)

        // RecyclerView 설정
        setupRecyclerView()

        // 카테고리 클릭 리스너
        setupCategoryListeners()

        // 프로필 버튼 클릭 리스너
        setupProfileButton()

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
        emptyStateContainer = view.findViewById(R.id.empty_state_container)
        btnProfile = view.findViewById(R.id.btn_profile)
    }

    private fun setupRecyclerView() {
        photoAdapter = PhotoGridAdapter()

        // 3열 그리드 레이아웃
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        rvPhotos.layoutManager = gridLayoutManager
        rvPhotos.adapter = photoAdapter

        // 아이템 간격 설정 (1dp)
        rvPhotos.addItemDecoration(GridSpacingItemDecoration(3, 1, false))  // 간격 1dp

        // 사진 클릭 리스너
        photoAdapter.setOnPhotoClickListener { photo ->
            // TODO: 사진 상세보기 화면으로 이동
            showToast("사진 클릭: ${photo.file.name}")
        }
    }

    private fun setupCategoryListeners() {
        categoryViews.forEach { (category, views) ->
            views.first.setOnClickListener {
                selectCategory(category)
            }
        }
    }

    /**
     * 프로필 버튼 클릭 리스너 설정
     */
    private fun setupProfileButton() {
        btnProfile.setOnClickListener {
            val intent = Intent(requireContext(), MyPageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun selectCategory(category: String) {
        selectedCategory = category

        // 모든 카테고리 업데이트
        categoryViews.forEach { (cat, views) ->
            val (container, imageView, textView) = views
            if (cat == category) {
                activateCategory(container, imageView, textView)
            } else {
                resetCategory(container, imageView, textView)
            }
        }

        // 사진 필터링
        loadPhotos()
    }

    private fun resetCategory(container: LinearLayout, imageView: ImageView, textView: TextView) {
        // 미선택 상태: opacity 40%
        container.alpha = 0.4f

        // 텍스트: gray_500, btn2(Medium)
        textView.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.gray_500)
        )
        textView.setTypeface(
            resources.getFont(R.font.pretendard_medium)
        )
    }

    private fun activateCategory(container: LinearLayout, imageView: ImageView, textView: TextView) {
        // 선택 상태: opacity 100%
        container.alpha = 1.0f

        // 텍스트: gray_50, btn2_B(Large)
        textView.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.gray_50)
        )
        textView.setTypeface(
            resources.getFont(R.font.pretendard_semibold)
        )
    }

    /**
     * 사진 로드 (서버 + 로컬 통합)
     */
    fun loadPhotos() {
        // 로그인 상태에 따라 다르게 처리
        if (tokenManager.isLoggedIn()) {
            loadServerPhotos()  // 로그인: 서버 사진
        } else {
            loadLocalPhotos()   // 비로그인: 로컬 사진
        }
    }

    /**
     * 서버에서 사진 로드 (로그인 사용자)
     */
    private fun loadServerPhotos() {
        // 카테고리 매핑
        val categoryCode = when (selectedCategory) {
            "공부" -> "STUDY"
            "운동" -> "EXERCISE"
            "음식" -> "FOOD"
            "기타" -> "ETC"
            else -> null  // 전체
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                imageRepository.getMyImages(
                    category = categoryCode,
                    page = 0,
                    size = 100
                ).onSuccess { response ->
                    Log.d("MyRecordsFragment", "서버 사진 ${response.images.size}개 로드됨")

                    if (response.images.isEmpty()) {
                        showEmptyState()
                    } else {
                        // ImageItem을 Photo로 변환
                        val photos = response.images.map { imageItem ->
                            // 더미 File 객체 (서버 URL만 사용)
                            Photo(
                                file = File(imageItem.imageId.toString()),
                                category = selectedCategory,
                                serverUrl = imageItem.accessUrl,
                                imageId = imageItem.imageId
                            )
                        }

                        photoAdapter.setPhotos(photos)
                        hideEmptyState()
                    }
                }.onFailure { error ->
                    Log.e("MyRecordsFragment", "서버 사진 로드 실패", error)
                    showEmptyState()
                    showToast("사진을 불러올 수 없습니다")
                }
            } catch (e: Exception) {
                Log.e("MyRecordsFragment", "사진 로드 오류", e)
                showEmptyState()
            }
        }
    }

    /**
     * 로컬에서 사진 로드 (비로그인 사용자)
     */
    private fun loadLocalPhotos() {
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

            Log.d("MyRecordsFragment", "로컬 사진 ${photos.size}개 로드됨")
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
        emptyStateContainer.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        rvPhotos.visibility = View.VISIBLE
        emptyStateContainer.visibility = View.GONE
    }
}

/**
 * 그리드 아이템 간격 조정 ItemDecoration
 */
class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position >= spanCount) {
                outRect.top = spacing
            }
        }
    }
}