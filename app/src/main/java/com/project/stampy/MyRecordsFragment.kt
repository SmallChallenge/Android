package com.project.stampy

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.stampy.data.local.NonLoginPhotoManager
import com.project.stampy.data.local.PhotoMetadataManager
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

    // 18장 도달 배너
    private lateinit var bannerPhotoLimit: View
    private lateinit var btnCloseBanner: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvPhotoCount: TextView
    private lateinit var tvBannerText: TextView

    private lateinit var photoAdapter: PhotoGridAdapter
    private var selectedCategory = "전체"

    // 서버 API 관련
    private lateinit var tokenManager: TokenManager
    private lateinit var imageRepository: ImageRepository
    private lateinit var nonLoginPhotoManager: NonLoginPhotoManager

    private lateinit var photoMetadataManager: PhotoMetadataManager

    // 배너 닫힘 상태 저장
    private var isBannerDismissed = false

    companion object {
        private const val PHOTO_LIMIT_BANNER_THRESHOLD = 18
    }

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
        nonLoginPhotoManager = NonLoginPhotoManager(requireContext())
        photoMetadataManager = PhotoMetadataManager(requireContext())

        // View 초기화
        initViews(view)

        // RecyclerView 설정
        setupRecyclerView()

        // 카테고리 클릭 리스너
        setupCategoryListeners()

        // 프로필 버튼 클릭 리스너
        setupProfileButton()

        // 배너 클릭 리스너
        setupBannerListeners()

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

        // 배너 뷰들
        bannerPhotoLimit = view.findViewById(R.id.banner_photo_limit)
        btnCloseBanner = bannerPhotoLimit.findViewById(R.id.btn_close_banner)
        progressBar = bannerPhotoLimit.findViewById(R.id.progress_bar)
        tvPhotoCount = bannerPhotoLimit.findViewById(R.id.tv_photo_count)
        tvBannerText = bannerPhotoLimit.findViewById(R.id.tv_banner_text)

        // 텍스트 볼드 처리 ("최대 20개")
        setupBannerText()
    }

    /**
     * 배너 텍스트 설정 ("최대 20개" 볼드 처리)
     */
    private fun setupBannerText() {
        val fullText = "게스트는 기록을 최대 20개까지 남길 수 있습니다."
        val boldText = "최대 20개"
        val spannableString = SpannableString(fullText)

        val startIndex = fullText.indexOf(boldText)
        if (startIndex >= 0) {
            spannableString.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                startIndex,
                startIndex + boldText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        tvBannerText.text = spannableString
    }

    /**
     * 배너 클릭 리스너
     */
    private fun setupBannerListeners() {
        btnCloseBanner.setOnClickListener {
            hideBanner()
            isBannerDismissed = true
        }
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
            navigateToPhotoDetail(photo)
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
            loadServerAndLocalPhotos()  // 로그인: 서버 사진 + 로컬 사진
        } else {
            loadLocalPhotos()   // 비로그인: 로컬 사진만
        }
    }

    /**
     * 18장 도달 시 배너 표시
     */
    private fun updatePhotoLimitBanner(photoCount: Int) {
        // 로그인 상태면 배너 숨김
        if (tokenManager.isLoggedIn()) {
            hideBanner()
            return
        }

        // 배너가 이미 닫혔으면 숨김 유지
        if (isBannerDismissed) {
            hideBanner()
            return
        }

        // 18장 이상이면 배너 표시
        if (photoCount >= PHOTO_LIMIT_BANNER_THRESHOLD) {
            showBanner(photoCount)
        } else {
            hideBanner()
        }
    }

    /**
     * 배너 표시
     */
    private fun showBanner(photoCount: Int) {
        bannerPhotoLimit.visibility = View.VISIBLE

        // 프로그레스 바 업데이트
        progressBar.progress = photoCount

        // 사진 개수 텍스트 업데이트
        tvPhotoCount.text = "$photoCount/20"
    }

    /**
     * 배너 숨김
     */
    private fun hideBanner() {
        bannerPhotoLimit.visibility = View.GONE
    }

    /**
     * 서버 + 로컬 사진 통합 로드 (로그인 사용자)
     */
    private fun loadServerAndLocalPhotos() {
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
                val photoMetadataManager = PhotoMetadataManager(requireContext())

                // 1. 서버 사진 로드
                val serverPhotos = mutableListOf<Photo>()

                imageRepository.getMyImages(
                    category = categoryCode,
                    page = 0,
                    size = 100
                ).onSuccess { response ->
                    Log.d("MyRecordsFragment", "서버 사진 ${response.images.size}개 로드됨")

                    serverPhotos.addAll(response.images.map { imageItem ->
                        // 서버의 originalTakenAt을 timestamp로 변환
                        val timestamp = parseIsoToTimestamp(imageItem.originalTakenAt)

                        // 서버에서 받은 카테고리를 한글로 변환
                        val categoryKorean = when (imageItem.category) {
                            "STUDY" -> "공부"
                            "EXERCISE" -> "운동"
                            "FOOD" -> "음식"
                            "ETC" -> "기타"
                            else -> "기타"
                        }

                        Photo(
                            file = File(imageItem.imageId.toString()),
                            category = categoryKorean,
                            serverUrl = imageItem.accessUrl,
                            imageId = imageItem.imageId,
                            timestamp = timestamp,
                            visibility = imageItem.visibility
                        )
                    })
                }.onFailure { error ->
                    Log.e("MyRecordsFragment", "서버 사진 로드 실패", error)
                }

                // 2. 로컬 사진 로드
                val localPhotos = loadLocalPhotosForLoggedInUser(categoryCode, photoMetadataManager)

                Log.d("MyRecordsFragment", "로컬 사진 ${localPhotos.size}개 로드됨")

                // 3. 서버 사진 + 로컬 사진 합치기
                // timestamp 기준으로 최신순 정렬
                val allPhotos = (serverPhotos + localPhotos)
                    .sortedByDescending { it.timestamp }

                // 4. UI 업데이트
                if (allPhotos.isEmpty()) {
                    showEmptyState()
                } else {
                    photoAdapter.setPhotos(allPhotos)
                    hideEmptyState()
                }

                Log.d("MyRecordsFragment", "전체 사진 ${allPhotos.size}개 표시 (서버: ${serverPhotos.size}, 로컬: ${localPhotos.size})")
            } catch (e: Exception) {
                Log.e("MyRecordsFragment", "사진 로드 오류", e)
                showEmptyState()
            }
        }
    }

    /**
     * ISO 8601 날짜 문자열을 timestamp(밀리초)로 변환
     * 예: "2024-12-30T12:00:00" → 1735516800000
     */
    private fun parseIsoToTimestamp(isoString: String): Long {
        return try {
            val formatter = java.time.format.DateTimeFormatter.ISO_DATE_TIME
            val dateTime = java.time.LocalDateTime.parse(isoString, formatter)
            dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            Log.e("MyRecordsFragment", "날짜 파싱 실패: $isoString", e)
            System.currentTimeMillis()  // 파싱 실패 시 현재 시간
        }
    }

    /**
     * 로그인 사용자용 로컬 사진 로드
     * (서버에 업로드되지 않은 비회원 시절 사진)
     */
    private fun loadLocalPhotosForLoggedInUser(
        categoryCode: String?,
        photoMetadataManager: PhotoMetadataManager
    ): List<Photo> {
        val picturesDir = File(requireContext().filesDir, "Pictures")

        if (!picturesDir.exists() || !picturesDir.isDirectory) {
            return emptyList()
        }

        // 1. 선택된 카테고리에 맞는 메타데이터 필터링
        val filteredMetadata = if (categoryCode == null) {
            // 전체 카테고리
            photoMetadataManager.getAllMetadata()
        } else {
            // 특정 카테고리
            photoMetadataManager.getMetadataByCategory(categoryCode)
        }

        // 2. 서버에 업로드되지 않은 로컬 사진만 필터링
        val localOnlyMetadata = filteredMetadata.filter { !it.isServerUploaded }

        // 3. 메타데이터에 해당하는 실제 파일만 로드
        return localOnlyMetadata.mapNotNull { metadata ->
            val file = File(picturesDir, metadata.fileName)
            if (file.exists()) {
                Photo(
                    file = file,
                    category = selectedCategory,
                    timestamp = metadata.createdAt
                )
            } else {
                null
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
            updatePhotoLimitBanner(0)  // 배너 업데이트
            return
        }

        // 1. 메타데이터 가져오기
        val photoMetadataManager = PhotoMetadataManager(requireContext())

        // 2. 선택된 카테고리에 맞는 메타데이터 필터링
        val filteredMetadata = if (selectedCategory == "전체") {
            photoMetadataManager.getAllMetadata()
        } else {
            // 카테고리 한글 → 영문 변환
            val categoryCode = when (selectedCategory) {
                "공부" -> "STUDY"
                "운동" -> "EXERCISE"
                "음식" -> "FOOD"
                "기타" -> "ETC"
                else -> null
            }

            if (categoryCode != null) {
                photoMetadataManager.getMetadataByCategory(categoryCode)
            } else {
                emptyList()
            }
        }

        // 3. 메타데이터에 해당하는 실제 파일만 로드
        val photos = filteredMetadata
            .mapNotNull { metadata ->
                val file = File(picturesDir, metadata.fileName)
                if (file.exists()) {
                    Photo(
                        file = file,
                        category = selectedCategory,
                        timestamp = metadata.createdAt
                    )
                } else {
                    null
                }
            }
            .sortedByDescending { it.timestamp }  // 최신순 정렬

        // 4. UI 업데이트
        if (photos.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
            photoAdapter.setPhotos(photos)
        }

        // 배너 업데이트 (전체 사진 개수 기준)
        val totalPhotoCount = nonLoginPhotoManager.getPhotoCount()
        updatePhotoLimitBanner(totalPhotoCount)

        Log.d("MyRecordsFragment", "비회원 로컬 사진 ${photos.size}개 로드됨 (전체: $totalPhotoCount)")
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

    /**
     * 사진 상세 화면으로 이동
     */
    private fun navigateToPhotoDetail(photo: Photo) {
        val intent = Intent(requireContext(), PhotoDetailActivity::class.java)

        // 서버 사진인 경우
        if (photo.imageId != null && photo.serverUrl != null) {
            intent.putExtra(PhotoDetailActivity.EXTRA_PHOTO_URL, photo.serverUrl)
            intent.putExtra(PhotoDetailActivity.EXTRA_IMAGE_ID, photo.imageId)

            // 서버 사진의 카테고리를 한글→영문으로 변환해서 전달
            val categoryCode = when (photo.category) {
                "공부" -> "STUDY"
                "운동" -> "EXERCISE"
                "음식" -> "FOOD"
                "기타" -> "ETC"
                else -> "ETC"
            }
            intent.putExtra(PhotoDetailActivity.EXTRA_CATEGORY, categoryCode)

            // 서버 사진의 공개 여부 전달 (이미 "PUBLIC" 또는 "PRIVATE" 형식)
            photo.visibility?.let { vis ->
                intent.putExtra(PhotoDetailActivity.EXTRA_VISIBILITY, vis)
            }
        }
        // 로컬 사진인 경우
        else if (photo.file.exists()) {
            intent.putExtra(PhotoDetailActivity.EXTRA_PHOTO_FILE, photo.file)

            // 메타데이터에서 카테고리 조회
            val metadata = photoMetadataManager.getMetadataByFileName(photo.file.name)

            if (metadata != null) {
                intent.putExtra(PhotoDetailActivity.EXTRA_CATEGORY, metadata.category)
                intent.putExtra(PhotoDetailActivity.EXTRA_VISIBILITY, metadata.visibility)
            } else {
                // 메타데이터가 없으면 Photo 객체의 카테고리 사용
                val categoryCode = when (photo.category) {
                    "공부" -> "STUDY"
                    "운동" -> "EXERCISE"
                    "음식" -> "FOOD"
                    "기타" -> "ETC"
                    else -> "ETC"
                }
                intent.putExtra(PhotoDetailActivity.EXTRA_CATEGORY, categoryCode)
                intent.putExtra(PhotoDetailActivity.EXTRA_VISIBILITY, "PRIVATE")
            }
        }

        startActivity(intent)
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