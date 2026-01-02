package com.project.stampy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.project.stampy.data.local.PhotoMetadataManager
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.ImageRepository
import com.project.stampy.etc.TagView
import com.project.stampy.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PhotoUpdateActivity : AppCompatActivity() {

    // 상단바
    private lateinit var btnBackTouchArea: FrameLayout
    private var btnComplete: MaterialButton? = null

    // 사진
    private lateinit var ivPhoto: ImageView

    // 카테고리
    private lateinit var categoryStudy: LinearLayout
    private lateinit var categoryExercise: LinearLayout
    private lateinit var categoryFood: LinearLayout
    private lateinit var categoryEtc: LinearLayout

    // 공개 여부 태그
    private lateinit var tagPublic: TagView
    private lateinit var tagPrivate: TagView

    // 안내 메시지
    private lateinit var tvPrivacyGuide: TextView
    private lateinit var warningLoginRequired: LinearLayout

    // 에러 메시지
    private lateinit var tvCategoryError: TextView
    private lateinit var tvPrivacyError: TextView

    // 로그인 관련
    private lateinit var tokenManager: TokenManager
    private lateinit var imageRepository: ImageRepository
    private lateinit var photoMetadataManager: PhotoMetadataManager

    private var selectedCategory: String? = null
    private var isPublic: Boolean? = null

    // 전달받은 데이터
    private var photoFile: File? = null
    private var photoUrl: String? = null
    private var imageId: Long? = null
    private var originalCategory: String? = null
    private var originalVisibility: String? = null

    private val categoryMap by lazy {
        mapOf(
            "공부" to Pair(categoryStudy, R.id.tv_category_study),
            "운동" to Pair(categoryExercise, R.id.tv_category_exercise),
            "음식" to Pair(categoryFood, R.id.tv_category_food),
            "기타" to Pair(categoryEtc, R.id.tv_category_etc)
        )
    }

    companion object {
        private const val TAG = "PhotoUpdateActivity"
        const val EXTRA_PHOTO_FILE = "extra_photo_file"
        const val EXTRA_PHOTO_URL = "extra_photo_url"
        const val EXTRA_IMAGE_ID = "extra_image_id"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_VISIBILITY = "extra_visibility"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_update)

        // 매니저 초기화
        tokenManager = TokenManager(this)
        RetrofitClient.initialize(tokenManager)
        imageRepository = ImageRepository(tokenManager)
        photoMetadataManager = PhotoMetadataManager(this)

        // Intent로 전달받은 데이터
        photoFile = intent.getSerializableExtra(EXTRA_PHOTO_FILE) as? File
        photoUrl = intent.getStringExtra(EXTRA_PHOTO_URL)
        imageId = intent.getLongExtra(EXTRA_IMAGE_ID, -1L).takeIf { it != -1L }
        originalCategory = intent.getStringExtra(EXTRA_CATEGORY)
        originalVisibility = intent.getStringExtra(EXTRA_VISIBILITY)

        initViews()
        setupListeners()
        loadPhotoData()
    }

    private fun initViews() {
        // 상단바
        btnBackTouchArea = findViewById(R.id.btn_back_touch_area)

        // 완료 버튼
        btnComplete = findViewById(R.id.btn_complete)
        btnComplete?.text = "완료"

        // 사진
        ivPhoto = findViewById(R.id.iv_photo)

        // 카테고리 LinearLayout
        categoryStudy = findViewById(R.id.category_study)
        categoryExercise = findViewById(R.id.category_exercise)
        categoryFood = findViewById(R.id.category_food)
        categoryEtc = findViewById(R.id.category_etc)

        // 공개 여부 태그
        tagPublic = findViewById(R.id.tag_public)
        tagPrivate = findViewById(R.id.tag_private)

        // 안내 메시지
        tvPrivacyGuide = findViewById(R.id.tv_privacy_guide)
        warningLoginRequired = findViewById(R.id.warning_login_required)

        // 에러 메시지
        tvCategoryError = findViewById(R.id.tv_category_error)
        tvPrivacyError = findViewById(R.id.tv_privacy_error)

        // UI 설정: 로컬 사진이거나 비로그인 유저인 경우
        val isLocalPhoto = imageId == null  // 서버 imageId가 없으면 로컬 사진

        if (isLocalPhoto) {
            // 로컬 사진: 로그인 여부와 관계없이 비공개만 가능
            tagPublic.visibility = View.GONE
            warningLoginRequired.visibility = View.VISIBLE
            tvPrivacyGuide.visibility = View.GONE
        } else if (!tokenManager.isLoggedIn()) {
            // 비로그인 유저 (서버 사진은 볼 수 없지만 방어 코드)
            tagPublic.visibility = View.GONE
            warningLoginRequired.visibility = View.VISIBLE
            tvPrivacyGuide.visibility = View.GONE
        } else {
            // 로그인 유저 + 서버 사진: 전체 공개 가능
            tagPublic.visibility = View.VISIBLE
            warningLoginRequired.visibility = View.GONE
            tvPrivacyGuide.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        // 뒤로가기
        btnBackTouchArea.setOnClickListener {
            finish()
        }

        // 완료 버튼
        btnComplete?.setOnClickListener {
            if (validateInputs()) {
                updatePhoto()
            }
        }

        // 카테고리 선택
        categoryStudy.setOnClickListener {
            selectCategory("공부")
            hideError(tvCategoryError)
        }
        categoryExercise.setOnClickListener {
            selectCategory("운동")
            hideError(tvCategoryError)
        }
        categoryFood.setOnClickListener {
            selectCategory("음식")
            hideError(tvCategoryError)
        }
        categoryEtc.setOnClickListener {
            selectCategory("기타")
            hideError(tvCategoryError)
        }

        // 공개 여부 태그
        tagPublic.setOnClickListener {
            selectPrivacy(true)
            hideError(tvPrivacyError)
        }
        tagPrivate.setOnClickListener {
            selectPrivacy(false)
            hideError(tvPrivacyError)
        }
    }

    /**
     * 사진 데이터 로드
     */
    private fun loadPhotoData() {
        // 사진 로드
        if (photoUrl != null) {
            // 서버 이미지
            Glide.with(this)
                .load(photoUrl)
                .centerCrop()
                .into(ivPhoto)
        } else if (photoFile != null && photoFile!!.exists()) {
            // 로컬 이미지
            Glide.with(this)
                .load(photoFile)
                .centerCrop()
                .into(ivPhoto)
        }

        // 카테고리 기본값 설정
        originalCategory?.let { cat ->
            val categoryKorean = when (cat) {
                "STUDY" -> "공부"
                "EXERCISE" -> "운동"
                "FOOD" -> "음식"
                "ETC" -> "기타"
                else -> "기타"
            }
            selectCategory(categoryKorean)
        }

        // 공개 여부 기본값 설정
        originalVisibility?.let { vis ->
            when (vis) {
                "PUBLIC" -> selectPrivacy(true)
                "PRIVATE" -> selectPrivacy(false)
            }
        }
    }

    /**
     * 카테고리 선택
     */
    private fun selectCategory(category: String) {
        selectedCategory = category

        // 모든 카테고리 순회하며 스타일 적용
        categoryMap.forEach { (name, views) ->
            val (container, textViewId) = views
            val textView = findViewById<TextView>(textViewId)

            if (name == category) {
                // 선택된 카테고리: opacity 100%, gray_50, Bold
                container.alpha = 1.0f
                textView?.apply {
                    setTextColor(ContextCompat.getColor(this@PhotoUpdateActivity, R.color.gray_50))
                    setTypeface(resources.getFont(R.font.pretendard_semibold))
                }
            } else {
                // 미선택 카테고리: opacity 40%, gray_500, Medium
                container.alpha = 0.4f
                textView?.apply {
                    setTextColor(ContextCompat.getColor(this@PhotoUpdateActivity, R.color.gray_500))
                    setTypeface(resources.getFont(R.font.pretendard_medium))
                }
            }
        }
    }

    /**
     * 공개 여부 선택
     */
    private fun selectPrivacy(isPublic: Boolean) {
        this.isPublic = isPublic

        if (isPublic) {
            // 전체 공개
            tagPublic.isSelected = true
            tagPrivate.isSelected = false
        } else {
            // 비공개
            tagPrivate.isSelected = true
            tagPublic.isSelected = false
        }
    }

    /**
     * 사진 수정
     */
    private fun updatePhoto() {
        val category = selectedCategory ?: run {
            showToast("카테고리를 선택해주세요")
            return
        }

        // 로딩 시작
        btnComplete?.isEnabled = false

        val categoryCode = mapCategoryToCode(category)
        val visibility = if (isPublic == true) "PUBLIC" else "PRIVATE"

        lifecycleScope.launch {
            try {
                // 로컬 사진 (비로그인 또는 서버 업로드 전)
                if (photoFile != null && imageId == null) {
                    updateLocalPhoto(photoFile!!.name, categoryCode, visibility)
                }
                // 서버 사진 (로그인 유저)
                else if (imageId != null) {
                    updateServerPhoto(imageId!!, categoryCode, visibility)
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("수정할 수 없는 사진입니다")
                        btnComplete?.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "사진 수정 실패", e)
                withContext(Dispatchers.Main) {
                    showToast("수정에 실패했어요. 다시 시도해 주세요.")
                    btnComplete?.isEnabled = true
                }
            }
        }
    }

    /**
     * 로컬 사진 수정 (메타데이터만 업데이트)
     */
    private suspend fun updateLocalPhoto(
        fileName: String,
        category: String,
        visibility: String
    ) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "로컬 사진 메타데이터 업데이트: $fileName")

            val metadata = photoMetadataManager.getMetadataByFileName(fileName)
            if (metadata != null) {
                val updatedMetadata = metadata.copy(
                    category = category,
                    visibility = visibility
                )
                photoMetadataManager.saveMetadata(updatedMetadata)

                withContext(Dispatchers.Main) {
                    showToast("수정이 완료되었어요.")
                    navigateToPhotoDetail()
                }
            } else {
                withContext(Dispatchers.Main) {
                    showToast("수정에 실패했어요. 다시 시도해 주세요.")
                    btnComplete?.isEnabled = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "로컬 사진 수정 실패", e)
            withContext(Dispatchers.Main) {
                showToast("수정에 실패했어요. 다시 시도해 주세요.")
                btnComplete?.isEnabled = true
            }
        }
    }

    /**
     * 서버 사진 수정
     */
    private suspend fun updateServerPhoto(
        imageId: Long,
        category: String,
        visibility: String
    ) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "서버 사진 수정 시작: imageId=$imageId, category=$category, visibility=$visibility")

            val result = imageRepository.updateImage(
                imageId = imageId,
                category = category,
                visibility = visibility
            )

            withContext(Dispatchers.Main) {
                result.onSuccess {
                    Log.d(TAG, "서버 사진 수정 성공")
                    showToast("수정이 완료되었어요.")
                    navigateToPhotoDetail()
                }.onFailure { error ->
                    Log.e(TAG, "서버 사진 수정 실패: ${error.message}", error)
                    showToast("수정에 실패했어요. 다시 시도해 주세요.")
                    btnComplete?.isEnabled = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "서버 사진 수정 오류", e)
            withContext(Dispatchers.Main) {
                showToast("수정에 실패했어요. 다시 시도해 주세요.")
                btnComplete?.isEnabled = true
            }
        }
    }

    /**
     * 사진 상세 화면으로 이동 (수정된 데이터 전달)
     */
    private fun navigateToPhotoDetail() {
        val intent = Intent(this, PhotoDetailActivity::class.java).apply {
            // 기존 액티비티 스택 정리
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            photoFile?.let { putExtra(PhotoDetailActivity.EXTRA_PHOTO_FILE, it) }
            photoUrl?.let { putExtra(PhotoDetailActivity.EXTRA_PHOTO_URL, it) }
            imageId?.let { putExtra(PhotoDetailActivity.EXTRA_IMAGE_ID, it) }

            // 수정된 카테고리와 공개여부 전달
            selectedCategory?.let {
                val categoryCode = mapCategoryToCode(it)
                putExtra(PhotoDetailActivity.EXTRA_CATEGORY, categoryCode)
            }
            isPublic?.let {
                val visibility = if (it) "PUBLIC" else "PRIVATE"
                putExtra(PhotoDetailActivity.EXTRA_VISIBILITY, visibility)
            }
        }

        startActivity(intent)
        finish()
    }

    /**
     * 카테고리 한글 → 영문 코드 변환
     */
    private fun mapCategoryToCode(category: String): String {
        return when (category) {
            "공부" -> "STUDY"
            "운동" -> "EXERCISE"
            "음식" -> "FOOD"
            "기타" -> "ETC"
            else -> "ETC"
        }
    }

    /**
     * 입력값 검증
     */
    private fun validateInputs(): Boolean {
        var isValid = true

        // 카테고리 검증
        if (selectedCategory == null) {
            showError(tvCategoryError)
            isValid = false
        } else {
            hideError(tvCategoryError)
        }

        // 공개 여부 검증
        if (isPublic == null) {
            showError(tvPrivacyError)
            isValid = false
        } else {
            hideError(tvPrivacyError)
        }

        return isValid
    }

    /**
     * 에러 메시지 표시
     */
    private fun showError(errorView: TextView) {
        errorView.visibility = View.VISIBLE
    }

    private fun hideError(errorView: TextView) {
        errorView.visibility = View.GONE
    }
}