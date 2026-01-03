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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.project.stampy.data.local.PhotoMetadataManager
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.ImageRepository
import com.project.stampy.etc.TagView
import com.project.stampy.etc.DoubleButtonDialog
import com.project.stampy.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PhotoDetailActivity : AppCompatActivity() {

    // 상단바
    private lateinit var btnBackTouchArea: FrameLayout
    private lateinit var btnMoreTouchArea: FrameLayout
    private lateinit var ivMore: ImageView

    // 팝오버 메뉴
    private lateinit var popoverMenu: ConstraintLayout
    private lateinit var menuEdit: LinearLayout
    private lateinit var menuDelete: LinearLayout

    // 사진
    private lateinit var ivPhoto: ImageView

    // 카테고리
    private lateinit var ivCategoryIcon: ImageView
    private lateinit var tvCategoryName: TextView

    // 공개 여부
    private lateinit var tagVisibility: TagView

    // 경고 메시지 (비로그인만)
    private lateinit var warningContainer: LinearLayout

    // 공유하기 버튼
    private lateinit var btnShare: MaterialButton

    // 데이터 관련
    private lateinit var tokenManager: TokenManager
    private lateinit var imageRepository: ImageRepository
    private lateinit var photoMetadataManager: PhotoMetadataManager

    private var photoFile: File? = null
    private var photoUrl: String? = null
    private var imageId: Long? = null
    private var category: String? = null
    private var visibility: String? = null

    companion object {
        private const val TAG = "PhotoDetailActivity"
        const val EXTRA_PHOTO_FILE = "extra_photo_file"
        const val EXTRA_PHOTO_URL = "extra_photo_url"
        const val EXTRA_IMAGE_ID = "extra_image_id"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_VISIBILITY = "extra_visibility"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_detail)

        // 매니저 초기화
        tokenManager = TokenManager(this)
        RetrofitClient.initialize(tokenManager)
        imageRepository = ImageRepository(tokenManager)
        photoMetadataManager = PhotoMetadataManager(this)

        // Intent로 전달받은 데이터
        photoFile = intent.getSerializableExtra(EXTRA_PHOTO_FILE) as? File
        photoUrl = intent.getStringExtra(EXTRA_PHOTO_URL)
        imageId = intent.getLongExtra(EXTRA_IMAGE_ID, -1L).takeIf { it != -1L }
        category = intent.getStringExtra(EXTRA_CATEGORY)
        visibility = intent.getStringExtra(EXTRA_VISIBILITY)

        initViews()
        setupListeners()
        loadPhotoData()
    }

    private fun initViews() {
        // 상단바
        btnBackTouchArea = findViewById(R.id.btn_back_touch_area)
        btnMoreTouchArea = findViewById(R.id.btn_more_touch_area)
        ivMore = findViewById(R.id.iv_more)

        // 팝오버 메뉴
        popoverMenu = findViewById(R.id.popover_menu)
        menuEdit = findViewById(R.id.menu_edit)
        menuDelete = findViewById(R.id.menu_delete)

        // 사진
        ivPhoto = findViewById(R.id.iv_photo)

        // 카테고리
        ivCategoryIcon = findViewById(R.id.iv_category_icon)
        tvCategoryName = findViewById(R.id.tv_category_name)

        // 공개 여부
        tagVisibility = findViewById(R.id.tag_visibility)

        // 경고 메시지
        warningContainer = findViewById(R.id.warning_container)

        // 공유하기 버튼
        btnShare = findViewById(R.id.btn_share)

        // 로컬 사진(로그인 전에 올린 사진)일 경우 경고 메시지 표시
        val isLocalPhoto = imageId == null  // 서버 imageId가 없으면 로컬 사진

        if (isLocalPhoto) {
            warningContainer.visibility = View.VISIBLE
        } else {
            warningContainer.visibility = View.GONE
            // 공유하기 버튼을 카테고리 아이콘 40dp 아래로 이동
            val constraintLayout = btnShare.parent as androidx.constraintlayout.widget.ConstraintLayout
            val constraintSet = androidx.constraintlayout.widget.ConstraintSet()
            constraintSet.clone(constraintLayout)
            constraintSet.connect(
                R.id.btn_share,
                androidx.constraintlayout.widget.ConstraintSet.TOP,
                R.id.category_container,
                androidx.constraintlayout.widget.ConstraintSet.BOTTOM,
                40
            )
            constraintSet.applyTo(constraintLayout)
        }
    }

    private fun setupListeners() {
        // 뒤로가기
        btnBackTouchArea.setOnClickListener {
            finish()
        }

        // 더보기 버튼 (팝오버 토글)
        btnMoreTouchArea.setOnClickListener {
            togglePopover()
        }

        // 팝오버 외부 클릭 시 닫기
        findViewById<View>(android.R.id.content).setOnClickListener {
            if (popoverMenu.visibility == View.VISIBLE) {
                hidePopover()
            }
        }

        // 기록 수정
        menuEdit.setOnClickListener {
            hidePopover()
            navigateToPhotoUpdate()
        }

        // 기록 삭제
        menuDelete.setOnClickListener {
            hidePopover()
            showDeleteConfirmDialog()
        }

        // 공유하기 버튼
        btnShare.setOnClickListener {
            sharePhoto()
        }
    }

    /**
     * 팝오버 토글
     */
    private fun togglePopover() {
        if (popoverMenu.visibility == View.VISIBLE) {
            hidePopover()
        } else {
            showPopover()
        }
    }

    /**
     * 팝오버 표시
     */
    private fun showPopover() {
        popoverMenu.visibility = View.VISIBLE
    }

    /**
     * 팝오버 숨김
     */
    private fun hidePopover() {
        popoverMenu.visibility = View.GONE
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

        // 카테고리 설정
        category?.let { cat ->
            setCategoryUI(cat)
        }

        // 공개 여부 설정
        visibility?.let { vis ->
            setVisibilityUI(vis)
        }
    }

    /**
     * 카테고리 UI 설정
     */
    private fun setCategoryUI(category: String) {
        when (category) {
            "STUDY" -> {
                ivCategoryIcon.setImageResource(R.drawable.ic_category_study)
                tvCategoryName.text = "공부"
            }
            "EXERCISE" -> {
                ivCategoryIcon.setImageResource(R.drawable.ic_category_exercise)
                tvCategoryName.text = "운동"
            }
            "FOOD" -> {
                ivCategoryIcon.setImageResource(R.drawable.ic_category_food)
                tvCategoryName.text = "음식"
            }
            "ETC" -> {
                ivCategoryIcon.setImageResource(R.drawable.ic_category_etc)
                tvCategoryName.text = "기타"
            }
        }
    }

    /**
     * 공개 여부 UI 설정
     */
    private fun setVisibilityUI(visibility: String) {
        // 클릭 비활성화 및 항상 선택 상태 유지
        tagVisibility.isClickable = false
        tagVisibility.isFocusable = false

        when (visibility) {
            "PUBLIC" -> {
                tagVisibility.text = "전체 공개"
                tagVisibility.isSelected = true
            }
            "PRIVATE" -> {
                tagVisibility.text = "비공개"
                tagVisibility.isSelected = true
            }
        }
    }

    /**
     * 사진 수정 화면으로 이동
     */
    private fun navigateToPhotoUpdate() {
        val intent = Intent(this, PhotoUpdateActivity::class.java).apply {
            photoFile?.let { putExtra(PhotoUpdateActivity.EXTRA_PHOTO_FILE, it) }
            photoUrl?.let { putExtra(PhotoUpdateActivity.EXTRA_PHOTO_URL, it) }
            imageId?.let { putExtra(PhotoUpdateActivity.EXTRA_IMAGE_ID, it) }
            category?.let { putExtra(PhotoUpdateActivity.EXTRA_CATEGORY, it) }
            visibility?.let { putExtra(PhotoUpdateActivity.EXTRA_VISIBILITY, it) }
        }
        startActivity(intent)
        finish()  // 현재 상세 화면 종료(수정 후 업데이트 된 새로운 상세 화면이 열림)
    }

    /**
     * 삭제 확인 다이얼로그 표시
     */
    private fun showDeleteConfirmDialog() {
        DoubleButtonDialog(this)
            .setTitle("사진을 삭제하시겠습니까?")
            .setCancelButtonText("취소")
            .setConfirmButtonText("삭제")
            .setOnCancelListener {
                Log.d(TAG, "삭제 취소")
            }
            .setOnConfirmListener {
                deletePhoto()
            }
            .show()
    }

    /**
     * 사진 삭제
     */
    private fun deletePhoto() {
        lifecycleScope.launch {
            try {
                if (tokenManager.isLoggedIn() && imageId != null) {
                    // 로그인 사용자 - 서버에서 삭제
                    deletePhotoFromServer(imageId!!)
                } else {
                    // 비로그인 사용자 - 로컬에서 삭제
                    deletePhotoLocally()
                }
            } catch (e: Exception) {
                Log.e(TAG, "사진 삭제 오류", e)
                showToast("삭제에 실패했어요. 다시 시도해 주세요.")
            }
        }
    }

    /**
     * 서버에서 사진 삭제 (로그인 사용자)
     */
    private suspend fun deletePhotoFromServer(imageId: Long) {
        imageRepository.deleteImage(imageId)
            .onSuccess { response ->
                Log.d(TAG, "서버 삭제 성공: ${response.message}")

                // 로컬 파일도 있다면 삭제
                photoFile?.let { file ->
                    if (file.exists()) {
                        file.delete()
                        photoMetadataManager.deleteMetadata(file.name)
                    }
                }

                showToast("삭제가 완료되었어요.")
                navigateToMyRecords()
            }
            .onFailure { error ->
                Log.e(TAG, "서버 삭제 실패", error)
                showToast("삭제에 실패했어요. 다시 시도해 주세요.")
            }
    }

    /**
     * 로컬에서 사진 삭제 (비로그인 사용자)
     */
    private fun deletePhotoLocally() {
        photoFile?.let { file ->
            if (file.exists()) {
                val deleted = file.delete()

                if (deleted) {
                    // 메타데이터 삭제
                    photoMetadataManager.deleteMetadata(file.name)

                    // 비로그인 사진 개수 감소
                    val nonLoginPhotoManager = com.project.stampy.data.local.NonLoginPhotoManager(this)
                    nonLoginPhotoManager.decrementPhotoCount()

                    Log.d(TAG, "로컬 삭제 성공: ${file.name}")
                    showToast("삭제가 완료되었어요.")
                    navigateToMyRecords()
                } else {
                    Log.e(TAG, "로컬 파일 삭제 실패")
                    showToast("삭제에 실패했어요. 다시 시도해 주세요.")
                }
            } else {
                Log.e(TAG, "파일이 존재하지 않음")
                showToast("삭제에 실패했어요. 다시 시도해 주세요.")
            }
        } ?: run {
            Log.e(TAG, "photoFile이 null")
            showToast("삭제에 실패했어요. 다시 시도해 주세요.")
        }
    }

    /**
     * 내 기록 화면으로 이동
     */
    private fun navigateToMyRecords() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("navigate_to_tab", "my_records")
        startActivity(intent)
        finish()
    }

    /**
     * 사진 공유하기
     */
    private fun sharePhoto() {
        lifecycleScope.launch {
            try {
                if (photoFile != null && photoFile!!.exists()) {
                    // 로컬 파일이 있는 경우
                    shareLocalFile(photoFile!!)
                } else if (photoUrl != null) {
                    // 서버 이미지 URL만 있는 경우 - 다운로드 후 공유
                    downloadAndShareImage(photoUrl!!)
                } else {
                    showToast("공유에 실패했어요. 다시 시도해 주세요.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "사진 공유 오류", e)
                showToast("공유에 실패했어요. 다시 시도해 주세요.")
            }
        }
    }

    /**
     * 로컬 파일 공유
     */
    private fun shareLocalFile(file: File) {
        try {
            // FileProvider를 사용하여 URI 생성
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            // 공유 Intent 생성
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // 공유 시트 표시
            startActivity(Intent.createChooser(shareIntent, "사진 공유하기"))

            Log.d(TAG, "로컬 파일 공유: ${file.name}")
        } catch (e: Exception) {
            Log.e(TAG, "로컬 파일 공유 오류", e)
            showToast("공유에 실패했어요. 다시 시도해 주세요.")
        }
    }

    /**
     * 서버 이미지 다운로드 후 공유
     * Glide의 디스크 캐시를 활용하여 중복 다운로드 방지
     */
    private suspend fun downloadAndShareImage(url: String) {
        withContext(Dispatchers.IO) {
            try {
                // Glide 캐시에서 파일 가져오기 (있으면 재사용, 없으면 다운로드)
                val file = Glide.with(this@PhotoDetailActivity)
                    .asFile()
                    .load(url)
                    .submit()
                    .get()

                // Glide 캐시 파일을 임시 파일로 복사 (FileProvider는 캐시 디렉토리 직접 접근 불가)
                val tempFile = File(cacheDir, "share_temp_${System.currentTimeMillis()}.jpg")
                file.copyTo(tempFile, overwrite = true)

                // 공유
                withContext(Dispatchers.Main) {
                    shareLocalFile(tempFile)

                    // 공유 후 임시 파일 삭제 예약 (5초 후)
                    tempFile.deleteOnExit()
                    lifecycleScope.launch {
                        kotlinx.coroutines.delay(5000)
                        if (tempFile.exists()) {
                            tempFile.delete()
                            Log.d(TAG, "임시 공유 파일 삭제: ${tempFile.name}")
                        }
                    }
                }

                Log.d(TAG, "서버 이미지 공유 (Glide 캐시 활용): $url")
            } catch (e: Exception) {
                Log.e(TAG, "서버 이미지 다운로드 실패", e)
                withContext(Dispatchers.Main) {
                    showToast("공유에 실패했어요. 다시 시도해 주세요.")
                }
            }
        }
    }
}