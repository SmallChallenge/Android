package com.project.stampy

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.project.stampy.auth.LoginActivity
import com.project.stampy.data.local.NonLoginPhotoManager
import com.project.stampy.data.local.PhotoMetadataManager
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.model.PhotoMetadata
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.ImageRepository
import com.project.stampy.etc.TagView
import com.project.stampy.etc.DoubleButtonDialog
import com.project.stampy.template.TemplateManager
import com.project.stampy.template.TemplateView
import com.project.stampy.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration

class PhotoSaveActivity : AppCompatActivity() {

    // 앰플리튜드
    private lateinit var amplitude: Amplitude

    // 상단바
    private lateinit var btnBackTouchArea: FrameLayout
    private var btnComplete: MaterialButton? = null

    // 사진
    private lateinit var ivPhoto: ImageView
    private lateinit var photoContainer: FrameLayout
    private lateinit var templateView: TemplateView
    private lateinit var tvTemplateOverlay: TextView

    // 카테고리
    private lateinit var categoryStudy: LinearLayout
    private lateinit var categoryExercise: LinearLayout
    private lateinit var categoryFood: LinearLayout
    private lateinit var categoryEtc: LinearLayout

    // 공개 여부 태그
    private lateinit var tagPublic: TagView
    private lateinit var tagPrivate: TagView

    // 에러 메시지
    private lateinit var tvCategoryError: TextView
    private lateinit var tvPrivacyError: TextView

    // 로그인 관련
    private lateinit var tokenManager: TokenManager
    private lateinit var imageRepository: ImageRepository
    private lateinit var nonLoginPhotoManager: NonLoginPhotoManager
    private lateinit var photoMetadataManager: PhotoMetadataManager

    private var selectedCategory: String? = null
    private var isPublic: Boolean? = null  // null: 미선택, true: 공개, false: 비공개

    private var photoUri: Uri? = null
    private var templateName: String? = null
    private var templateId: String? = null
    private var showLogo: Boolean = true

    private val categoryMap by lazy {
        mapOf(
            "공부" to Pair(categoryStudy, R.id.tv_category_study),
            "운동" to Pair(categoryExercise, R.id.tv_category_exercise),
            "음식" to Pair(categoryFood, R.id.tv_category_food),
            "기타" to Pair(categoryEtc, R.id.tv_category_etc)
        )
    }

    companion object {
        private const val TAG = "PhotoSaveActivity"
        const val EXTRA_PHOTO_URI = "extra_photo_uri"
        const val EXTRA_TEMPLATE_NAME = "extra_template_name"
        const val EXTRA_TEMPLATE_ID = "extra_template_id"
        const val EXTRA_SHOW_LOGO = "extra_show_logo"

        private const val TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss"
        private const val FILE_PREFIX = "STAMPIC_"  // "Stampic" 파일명으로 저장
        private const val FILE_EXTENSION = ".jpg"

        fun generateFileName(): String {
            val timestamp = SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault())
                .format(Date())
            return "$FILE_PREFIX$timestamp$FILE_EXTENSION"
        }
    }

    // 로그인 결과 처리
    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // 로그인 성공 - 전체 공개 상태로만 설정
            Log.d(TAG, "로그인 성공 - 전체 공개 상태로 설정")
            selectPrivacy(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_save)

        // 매니저 초기화
        tokenManager = TokenManager(this)
        RetrofitClient.initialize(tokenManager)
        imageRepository = ImageRepository(tokenManager)
        nonLoginPhotoManager = NonLoginPhotoManager(this)
        photoMetadataManager = PhotoMetadataManager(this)

        // 앰플리튜드 초기화 및 유저 식별
        amplitude = Amplitude(Configuration(getString(R.string.amplitude_api_key), applicationContext))
        if (tokenManager.isLoggedIn()) {
            val savedUserId = tokenManager.getUserId()
            if (savedUserId != -1L) {
                amplitude.setUserId("user_$savedUserId")
            }
        }

        // Intent로 전달받은 데이터
        photoUri = intent.getParcelableExtra(EXTRA_PHOTO_URI)
        templateName = intent.getStringExtra(EXTRA_TEMPLATE_NAME)
        templateId = intent.getStringExtra(EXTRA_TEMPLATE_ID)
        showLogo = intent.getBooleanExtra(EXTRA_SHOW_LOGO, true)

        Log.d(TAG, "Received showLogo: $showLogo")  // 로그 추가

        initViews()
        setupListeners()
        loadPhoto()
        applyTemplate()
    }

    private fun trackPhotoSaveComplete() {
        val commonProps = mutableMapOf(
            "is_logged_in" to tokenManager.isLoggedIn(),
            "platform" to "android",
            "app_version" to "1.0.0",
            "category" to (selectedCategory ?: "기타"),
            "save_scope" to if (isPublic == true) "public" else "private",
            "template_id" to (templateId ?: "none"),
            "is_logo_shown" to showLogo
        )

        amplitude.track("photo_save_complete", commonProps)
    }

    private fun initViews() {
        // 상단바
        btnBackTouchArea = findViewById(R.id.btn_back_touch_area)

        // 완료 버튼
        btnComplete = findViewById(R.id.btn_complete)
        btnComplete?.text = "완료"

        // 사진 컨테이너
        photoContainer = findViewById(R.id.photo_container)
        ivPhoto = findViewById(R.id.iv_photo)
        tvTemplateOverlay = findViewById(R.id.tv_template_overlay)

        // 템플릿 뷰 추가
        templateView = TemplateView(this)
        photoContainer.addView(templateView)

        // 카테고리 LinearLayout
        categoryStudy = findViewById(R.id.category_study)
        categoryExercise = findViewById(R.id.category_exercise)
        categoryFood = findViewById(R.id.category_food)
        categoryEtc = findViewById(R.id.category_etc)

        // 공개 여부 태그
        tagPublic = findViewById(R.id.tag_public)
        tagPrivate = findViewById(R.id.tag_private)

        // 에러 메시지
        tvCategoryError = findViewById(R.id.tv_category_error)
        tvPrivacyError = findViewById(R.id.tv_privacy_error)
    }

    private fun setupListeners() {
        // 뒤로가기
        btnBackTouchArea.setOnClickListener {
            finish()
        }

        // 완료 버튼
        btnComplete?.setOnClickListener {
            if (validateInputs()) {
                savePhoto()
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
            // 비로그인 유저가 전체 공개 선택 시 로그인 요청
            if (!tokenManager.isLoggedIn()) {
                showLoginRequiredDialog()
            } else {
                selectPrivacy(true)
                hideError(tvPrivacyError)
            }
        }
        tagPrivate.setOnClickListener {
            selectPrivacy(false)
            hideError(tvPrivacyError)
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
                    setTextColor(ContextCompat.getColor(this@PhotoSaveActivity, R.color.gray_50))
                    setTypeface(resources.getFont(R.font.pretendard_semibold))
                }
            } else {
                // 미선택 카테고리: opacity 40%, gray_500, Medium
                container.alpha = 0.4f
                textView?.apply {
                    setTextColor(ContextCompat.getColor(this@PhotoSaveActivity, R.color.gray_500))
                    setTypeface(resources.getFont(R.font.pretendard_medium))
                }
            }
        }
    }

    /**
     * 로그인 필요 다이얼로그 표시
     */
    private fun showLoginRequiredDialog() {
        DoubleButtonDialog(this)
            .setTitle("로그인이 필요해요.")
            .setCancelButtonText("취소")
            .setConfirmButtonText("로그인")
            .setOnCancelListener {
                Log.d(TAG, "로그인 취소")
            }
            .setOnConfirmListener {
                Log.d(TAG, "로그인 화면으로 이동")
                navigateToLogin()
            }
            .show()
    }

    /**
     * 로그인 화면으로 이동
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra(LoginActivity.EXTRA_RETURN_TO_PHOTO_SAVE, true)
        }
        loginLauncher.launch(intent)
    }

    /**
     * 사진 저장 로직
     */
    private fun savePhoto() {
        val uri = photoUri ?: run {
            showToast("사진 정보가 없어요.")
            return
        }

        val category = selectedCategory ?: run {
            showToast("카테고리를 선택해주세요")
            return
        }

        val isLoggedIn = tokenManager.isLoggedIn()

        // 비로그인 유저 - 20장 제한 체크
        if (!isLoggedIn) {
            if (!nonLoginPhotoManager.canSaveMorePhotos()) {
                showToast("비로그인 상태에서는 최대 ${nonLoginPhotoManager.getMaxPhotos()}장까지 저장 가능합니다")
                return
            }
        }

        // 로딩 시작
        btnComplete?.isEnabled = false

        lifecycleScope.launch {
            try {
                // 1. 템플릿 적용된 최종 이미지 생성
                val finalBitmap = createFinalImage(uri) ?: run {
                    showToast("저장에 실패했어요. 다시 시도해 주세요.")
                    btnComplete?.isEnabled = true
                    return@launch
                }

                val fileName = generateFileName()
                val categoryCode = mapCategoryToCode(category)
                val visibility = if (isPublic == true) "PUBLIC" else "PRIVATE"

                if (isLoggedIn) {
                    // 로그인 유저: 갤러리 + 서버 저장
                    savePhotoForLoggedInUser(finalBitmap, fileName, categoryCode, visibility)
                } else {
                    // 비로그인 유저: 로컬 + 갤러리 저장
                    savePhotoForNonLoggedInUser(finalBitmap, fileName, categoryCode, visibility)
                }

            } catch (e: Exception) {
                Log.e(TAG, "사진 저장 실패", e)
                showToast("저장에 실패했어요. 다시 시도해 주세요.: ${e.message}")
                btnComplete?.isEnabled = true
            }
        }
    }

    /**
     * 비로그인 유저 - 사진 저장
     * 로컬 + 갤러리
     */
    private suspend fun savePhotoForNonLoggedInUser(
        bitmap: Bitmap,
        fileName: String,
        category: String,
        visibility: String
    ) {
        Log.d(TAG, "비로그인 유저 저장: 로컬 + 갤러리")

        // 1. 로컬에 저장
        val localFile = saveToLocal(bitmap, fileName)
        if (localFile == null) {
            withContext(Dispatchers.Main) {
                showToast("저장에 실패했어요. 다시 시도해 주세요.")
                btnComplete?.isEnabled = true
            }
            return
        }

        // 2. 갤러리에 저장
        val galleryUri = saveToGallery(bitmap, fileName)
        if (galleryUri == null) {
            withContext(Dispatchers.Main) {
                showToast("저장에 실패했어요. 다시 시도해 주세요.")
                btnComplete?.isEnabled = true
            }
            return
        }

        // 3. 메타데이터 저장
        val metadata = PhotoMetadata(
            fileName = fileName,
            category = category,
            visibility = visibility,
            createdAt = System.currentTimeMillis(),
            templateName = templateName,
            isServerUploaded = false
        )
        photoMetadataManager.saveMetadata(metadata)

        // 4. 카운트 증가
        nonLoginPhotoManager.incrementPhotoCount()

        Log.d(TAG, "비로그인 저장 완료: ${nonLoginPhotoManager.getPhotoCount()}/${nonLoginPhotoManager.getMaxPhotos()}")

        // 앰플리튜드 이벤트 전송
        trackPhotoSaveComplete()

        withContext(Dispatchers.Main) {
            showToast("저장이 완료되었어요.")
            navigateToMain()
        }
    }

    /**
     * 로그인 유저 - 사진 저장
     * 갤러리 + 서버 (+ 전체공개시 커뮤니티)
     */
    private suspend fun savePhotoForLoggedInUser(
        bitmap: Bitmap,
        fileName: String,
        category: String,
        visibility: String
    ) {
        Log.d(TAG, "로그인 유저 저장: 갤러리 + 서버")

        // 1. 갤러리에 저장
        val galleryUri = saveToGallery(bitmap, fileName)
        if (galleryUri == null) {
            withContext(Dispatchers.Main) {
                showToast("저장에 실패했어요. 다시 시도해 주세요.")
                btnComplete?.isEnabled = true
            }
            return
        }

        // 2. 임시 파일 생성 (서버 업로드용)
        val tempFile = saveTempFile(bitmap, fileName)
        if (tempFile == null) {
            withContext(Dispatchers.Main) {
                showToast("요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.")
                btnComplete?.isEnabled = true
            }
            return
        }

        // 3. 메타데이터 저장 (서버 업로드 전)
        val metadata = PhotoMetadata(
            fileName = fileName,
            category = category,
            visibility = visibility,
            createdAt = System.currentTimeMillis(),
            templateName = templateName,
            isServerUploaded = false
        )
        photoMetadataManager.saveMetadata(metadata)

        // 4. 서버 업로드
        uploadToServer(tempFile, category, visibility, fileName)
    }

    /**
     * 템플릿 적용된 최종 이미지 생성
     */
    private suspend fun createFinalImage(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // 화면에 보이는 그대로 캡처 (템플릿 포함)
            val finalBitmap = withContext(Dispatchers.Main) {
                // 템플릿 오버레이 텍스트 숨기기
                tvTemplateOverlay.visibility = View.GONE

                // 캡처
                val bitmap = captureViewAsBitmap()

                bitmap
            }

            if (finalBitmap != null) {
                // 1:1 크롭
                val croppedBitmap = cropToSquare(finalBitmap)

                // 최대 크기 제한
                val resizedBitmap = resizeIfNeeded(croppedBitmap, maxSize = 2048)

                // 메모리 정리
                if (croppedBitmap != finalBitmap && croppedBitmap != resizedBitmap) {
                    croppedBitmap.recycle()
                }
                if (finalBitmap != resizedBitmap) {
                    finalBitmap.recycle()
                }

                return@withContext resizedBitmap
            }

            // 폴백: 화면 캡처 실패 시 원본 이미지 처리
            Log.w(TAG, "화면 캡처 실패, 원본 이미지로 처리")
            val originalBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val rotatedBitmap = fixImageRotation(uri, originalBitmap)
            val croppedBitmap = cropToSquare(rotatedBitmap)
            val resizedBitmap = resizeIfNeeded(croppedBitmap, maxSize = 2048)

            // 메모리 정리
            if (rotatedBitmap != originalBitmap) originalBitmap.recycle()
            if (croppedBitmap != rotatedBitmap && croppedBitmap != resizedBitmap) croppedBitmap.recycle()
            if (rotatedBitmap != resizedBitmap) rotatedBitmap.recycle()

            resizedBitmap
        } catch (e: Exception) {
            Log.e(TAG, "이미지 생성 실패", e)
            null
        }
    }

    /**
     * 화면 캡처
     */
    private fun captureViewAsBitmap(): Bitmap? {
        try {
            val parentView = ivPhoto.parent as? View ?: return null

            // 뷰가 그려지지 않았으면 null 반환
            if (parentView.width == 0 || parentView.height == 0) {
                Log.w(TAG, "뷰 크기가 0, 캡처 불가")
                return null
            }

            // 비트맵 생성
            val bitmap = Bitmap.createBitmap(
                parentView.width,
                parentView.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            parentView.draw(canvas)

            Log.d(TAG, "화면 캡처 완료: ${parentView.width}x${parentView.height}")
            return bitmap
        } catch (e: Exception) {
            Log.e(TAG, "화면 캡처 실패", e)
            return null
        }
    }

    /**
     * EXIF 정보를 읽어서 이미지 회전 수정
     */
    private fun fixImageRotation(uri: Uri, bitmap: Bitmap): Bitmap {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return bitmap
            val exif = androidx.exifinterface.media.ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
            )

            val matrix = android.graphics.Matrix()
            when (orientation) {
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return bitmap
            }

            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            Log.d(TAG, "이미지 회전 처리: orientation=$orientation")
            return rotatedBitmap
        } catch (e: Exception) {
            Log.e(TAG, "회전 처리 실패", e)
            return bitmap
        }
    }

    /**
     * 1:1 정사각형으로 크롭 (중앙 기준)
     */
    private fun cropToSquare(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width == height) {
            return bitmap
        }

        val size = minOf(width, height)
        val x = (width - size) / 2
        val y = (height - size) / 2

        val croppedBitmap = Bitmap.createBitmap(bitmap, x, y, size, size)
        Log.d(TAG, "1:1 크롭 완료: ${width}x${height} -> ${size}x${size}")
        return croppedBitmap
    }

    /**
     * 최대 크기 제한 (메모리 및 용량 절약)
     */
    private fun resizeIfNeeded(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val scale = maxSize.toFloat() / maxOf(width, height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        Log.d(TAG, "리사이즈 완료: ${width}x${height} -> ${newWidth}x${newHeight}")
        return resizedBitmap
    }

    /**
     * 로컬(앱 내부)에 저장 - 비로그인 유저만
     */
    private suspend fun saveToLocal(bitmap: Bitmap, fileName: String): File? = withContext(Dispatchers.IO) {
        try {
            val picturesDir = File(filesDir, "Pictures").apply {
                if (!exists()) mkdirs()
            }

            val file = File(picturesDir, fileName)
            FileOutputStream(file).use { out ->
                // iOS와 동일하게 품질 85% (용량 최적화)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            Log.d(TAG, "로컬 저장 성공: ${file.absolutePath}, 크기: ${file.length() / 1024}KB")
            file
        } catch (e: Exception) {
            Log.e(TAG, "로컬 저장 실패", e)
            null
        }
    }

    /**
     * 임시 파일 저장 - 서버 업로드용 (로그인 유저)
     */
    private suspend fun saveTempFile(bitmap: Bitmap, fileName: String): File? = withContext(Dispatchers.IO) {
        try {
            val tempFile = File(cacheDir, fileName)
            FileOutputStream(tempFile).use { out ->
                // iOS와 동일하게 품질 85% (용량 최적화)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            Log.d(TAG, "임시 파일 생성 성공: ${tempFile.absolutePath}, 크기: ${tempFile.length() / 1024}KB")
            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "임시 파일 생성 실패", e)
            null
        }
    }

    /**
     * 갤러리 저장
     */
    private suspend fun saveToGallery(bitmap: Bitmap, fileName: String): Uri? = withContext(Dispatchers.IO) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/스탬픽")  // "스탬픽"앨범명(폴더명)으로 저장
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    // iOS와 동일하게 품질 85% (용량 최적화)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                }

                Log.d(TAG, "갤러리 저장 성공: $uri")
                return@withContext uri
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "갤러리 저장 실패", e)
            null
        }
    }

    /**
     * 서버에 업로드 (Swagger API 참고) - 로그인 유저만
     */
    private suspend fun uploadToServer(
        file: File,
        category: String,
        visibility: String,
        fileName: String
    ) = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "서버 업로드 시작: category=$category, visibility=$visibility")

            // 파일 이름에서 타임스탬프 추출
            val timestamp = extractTimestampFromFileName(fileName)

            val result = imageRepository.uploadImage(
                imageFile = file,
                category = category,
                visibility = visibility,
                takenAtTimestamp = timestamp
            )

            withContext(Dispatchers.Main) {
                result.onSuccess { response ->
                    Log.d(TAG, "서버 업로드 성공: imageId=${response.imageId}")

                    // 1. 앰플리튜드 이벤트 전송 (공통 저장 완료)
                    trackPhotoSaveComplete()

                    // 2. 전체 공개인 경우 '커뮤니티 게시물 생성' 이벤트 추가 전송
                    if (visibility == "PUBLIC") {
                        Log.d(TAG, "전체 공개 - 커뮤니티에 자동 게시됨")
                        amplitude.track("post_create_complete", mapOf(
                            "is_logged_in" to true,
                            "platform" to "android"
                        ))
                    }

                    // 메타데이터 업데이트
                    photoMetadataManager.updateServerUploadStatus(fileName, true)

                    // 임시 파일 삭제
                    file.delete()

                    // 전체 공개인 경우 자동으로 커뮤니티에 게시됨
                    if (visibility == "PUBLIC") {
                        Log.d(TAG, "전체 공개 - 커뮤니티에 자동 게시됨")
                    }

                    showToast("저장이 완료되었어요.")
                    navigateToMain()

                }.onFailure { error ->
                    Log.e(TAG, "서버 업로드 실패: ${error.message}", error)
                    // 서버 업로드 실패해도 갤러리는 저장됨
                    file.delete()
                    showToast("갤러리 저장이 완료되었어요. (서버 업로드 실패)")
                    navigateToMain()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e(TAG, "서버 업로드 오류", e)
                file.delete()
                showToast("갤러리 저장 완료이 완료되었어요. (서버 업로드 실패)")
                navigateToMain()
            }
        }
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
     * 메인 화면으로 이동 (항상 내 기록 탭으로)
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        // 내 기록 탭으로 이동하도록 플래그 설정
        intent.putExtra(MainActivity.EXTRA_NAVIGATE_TO_STORAGE, true)
        startActivity(intent)
        finish()
    }

    /**
     * 입력값 검증
     */
    private fun validateInputs(): Boolean {
        var isValid = true
        var shouldShowToast = false

        // 카테고리 검증
        if (selectedCategory == null) {
            showError(tvCategoryError)
            isValid = false
            shouldShowToast = true
        } else {
            hideError(tvCategoryError)
        }

        // 공개 여부 검증
        if (isPublic == null) {
            showError(tvPrivacyError)
            isValid = false
            shouldShowToast = true
        } else {
            hideError(tvPrivacyError)
        }

        if (shouldShowToast) {
            showToast("필수 항목을 선택해 주세요.")
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

    /**
     * 사진 로드
     */
    private fun loadPhoto() {
        photoUri?.let { uri ->
            Log.d(TAG, "Loading photo: $uri")

            // 배경 ivPhoto에 로드
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(ivPhoto)

            // TemplateView의 iv_photo에도 로드 (Moody 3 폴라로이드용)
            Glide.with(this)
                .asBitmap()
                .load(uri)
                .centerCrop()
                .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                    ) {
                        templateView.setPhoto(resource)
                        Log.d(TAG, "Template photo loaded for save: ${resource.width}x${resource.height}")
                    }

                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                        // Do nothing
                    }
                })
        } ?: Log.e(TAG, "photoUri is null")
    }

    /**
     * 템플릿 적용
     */
    private fun applyTemplate() {
        templateId?.let { id ->
            val template = TemplateManager.getTemplateById(id)
            template?.let {
                templateView.applyTemplate(it, showLogo = showLogo)  // showLogo 변수 사용
                Log.d(TAG, "템플릿 적용: ${it.name}, 로고 표시: $showLogo")
            } ?: Log.e(TAG, "템플릿을 찾을 수 없습니다: $id")
        } ?: Log.w(TAG, "템플릿 ID가 없습니다")
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
     * 파일명에서 타임스탬프 추출
     * 예: STAMPIC_20260103_152745.jpg → 1735905596000 (Unix timestamp)
     */
    private fun extractTimestampFromFileName(fileName: String): Long {
        return try {
            // STAMPIC_20260103_152745.jpg → 20260103_152745
            val timestampStr = fileName
                .removePrefix(FILE_PREFIX)
                .removeSuffix(FILE_EXTENSION)

            val sdf = SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault())
            val date = sdf.parse(timestampStr)
            date?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            Log.e(TAG, "파일명에서 타임스탬프 추출 실패", e)
            System.currentTimeMillis()
        }
    }
}