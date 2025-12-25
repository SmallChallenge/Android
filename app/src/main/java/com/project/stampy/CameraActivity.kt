package com.project.stampy

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project.stampy.utils.showToast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    // 상단바
    private lateinit var btnBackTouchArea: FrameLayout

    // 카메라
    private lateinit var previewView: PreviewView

    // 갤러리
    private lateinit var rvGallery: RecyclerView
    private lateinit var galleryAdapter: GalleryAdapter

    // 촬영 버튼
    private lateinit var captureArea: View
    private lateinit var btnCapture: ImageView
    private lateinit var btnSwitchCameraTouchArea: FrameLayout
    private lateinit var ivSwitchCamera: ImageView
    private lateinit var btnFlashTouchArea: FrameLayout
    private lateinit var ivFlash: ImageView

    // 하단 버튼
    private lateinit var btnGallery: TextView
    private lateinit var btnCamera: TextView

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private lateinit var cameraExecutor: ExecutorService

    // 카메라 상태
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var flashMode = ImageCapture.FLASH_MODE_OFF

    // 현재 탭 (true: 카메라, false: 갤러리)
    private var isCameraMode = true

    private val CAMERA_PERMISSION_CODE = 101
    private val GALLERY_PERMISSION_CODE = 102

    companion object {
        private const val TAG = "CameraActivity"
        private const val TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss"
        private const val FILE_PREFIX = "STAMPY_"
        private const val FILE_EXTENSION = ".jpg"

        fun generateFileName(): String {
            val timestamp = SimpleDateFormat(TIMESTAMP_FORMAT, Locale.getDefault())
                .format(Date())
            return "$FILE_PREFIX$timestamp$FILE_EXTENSION"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        initViews()
        setupListeners()
        setupGalleryRecyclerView()

        cameraExecutor = Executors.newSingleThreadExecutor()

        // 카메라 권한 확인
        if (allCameraPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        }
    }

    private fun initViews() {
        // 상단바
        btnBackTouchArea = findViewById(R.id.btn_back_touch_area)

        // 카메라
        previewView = findViewById(R.id.preview_view)

        // 갤러리
        rvGallery = findViewById(R.id.rv_gallery)

        // 촬영 버튼 영역
        captureArea = findViewById(R.id.capture_area)
        btnCapture = findViewById(R.id.btn_capture)
        btnSwitchCameraTouchArea = findViewById(R.id.btn_switch_camera_touch_area)
        ivSwitchCamera = findViewById(R.id.iv_switch_camera)
        btnFlashTouchArea = findViewById(R.id.btn_flash_touch_area)
        ivFlash = findViewById(R.id.iv_flash)

        // 하단 버튼
        btnGallery = findViewById(R.id.btn_gallery)
        btnCamera = findViewById(R.id.btn_camera)
    }

    private fun setupListeners() {
        // 뒤로가기
        btnBackTouchArea.setOnClickListener {
            finish()
        }

        // 촬영
        btnCapture.setOnClickListener {
            takePhoto()
        }

        // 전면/후면 전환
        btnSwitchCameraTouchArea.setOnClickListener {
            switchCamera()
        }

        // 플래시 전환
        btnFlashTouchArea.setOnClickListener {
            toggleFlash()
        }

        // 갤러리 버튼
        btnGallery.setOnClickListener {
            switchToGalleryMode()
        }

        // 카메라 버튼
        btnCamera.setOnClickListener {
            switchToCameraMode()
        }
    }

    /**
     * 갤러리 RecyclerView 설정
     */
    private fun setupGalleryRecyclerView() {
        galleryAdapter = GalleryAdapter()

        // 3열 그리드 레이아웃
        val gridLayoutManager = GridLayoutManager(this, 3)
        rvGallery.layoutManager = gridLayoutManager
        rvGallery.adapter = galleryAdapter

        // 아이템 간격 설정 (1dp)
        rvGallery.addItemDecoration(CameraGridSpacingItemDecoration(3, 1, false))

        // 사진 클릭 리스너
        galleryAdapter.setOnPhotoClickListener { uri ->
            // TODO: 사진 편집 화면으로 이동
            showToast("사진 편집 기능은 곧 추가됩니다")
        }
    }

    /**
     * 갤러리 모드로 전환
     */
    private fun switchToGalleryMode() {
        if (!isCameraMode) return

        // 갤러리 권한 확인
        if (!allGalleryPermissionsGranted()) {
            requestGalleryPermission()
            return
        }

        isCameraMode = false

        // UI 업데이트
        previewView.visibility = View.GONE
        captureArea.visibility = View.GONE
        rvGallery.visibility = View.VISIBLE

        // 버튼 상태 변경
        btnGallery.setTextColor(ContextCompat.getColor(this, R.color.gray_50))
        btnCamera.setTextColor(ContextCompat.getColor(this, R.color.gray_500))

        // 갤러리 사진 로드
        loadGalleryPhotos()
    }

    /**
     * 카메라 모드로 전환
     */
    private fun switchToCameraMode() {
        if (isCameraMode) return

        isCameraMode = true

        // UI 업데이트
        rvGallery.visibility = View.GONE
        previewView.visibility = View.VISIBLE
        captureArea.visibility = View.VISIBLE

        // 버튼 상태 변경
        btnCamera.setTextColor(ContextCompat.getColor(this, R.color.gray_50))
        btnGallery.setTextColor(ContextCompat.getColor(this, R.color.gray_500))
    }

    /**
     * 갤러리 사진 로드
     */
    private fun loadGalleryPhotos() {
        val photos = mutableListOf<Uri>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )
                photos.add(uri)
            }
        }

        galleryAdapter.setPhotos(photos)
        Log.d(TAG, "갤러리 사진 ${photos.size}개 로드됨")
    }

    /**
     * 카메라 시작
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview 설정
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // ImageCapture 설정
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setFlashMode(flashMode)
                .build()

            // 카메라 선택 (전면/후면)
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e(TAG, "카메라 시작 실패", e)
                showToast("카메라 시작 실패")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * 전면/후면 카메라 전환
     */
    private fun switchCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCamera()
    }

    /**
     * 플래시 모드 전환 (OFF → AUTO → ON → OFF)
     */
    private fun toggleFlash() {
        flashMode = when (flashMode) {
            ImageCapture.FLASH_MODE_OFF -> {
                ivFlash.setImageResource(R.drawable.ic_flash_auto)
                ImageCapture.FLASH_MODE_AUTO
            }
            ImageCapture.FLASH_MODE_AUTO -> {
                ivFlash.setImageResource(R.drawable.ic_flash_on)
                ImageCapture.FLASH_MODE_ON
            }
            else -> {
                ivFlash.setImageResource(R.drawable.ic_flash_off)
                ImageCapture.FLASH_MODE_OFF
            }
        }

        // ImageCapture 플래시 모드 업데이트
        imageCapture?.flashMode = flashMode
    }

    /**
     * 사진 촬영
     */
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // 1. 앱 내부 저장소에 저장
        val appFile = createAppPhotoFile()
        val appOutputOptions = ImageCapture.OutputFileOptions.Builder(appFile).build()

        imageCapture.takePicture(
            appOutputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "앱 저장소 저장: ${appFile.absolutePath}")

                    // 2. 갤러리(공용 저장소)에도 저장
                    saveToGallery(appFile)
                    showToast("사진 저장 완료!")

                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    showToast("사진 저장 실패: ${exception.message}")
                    Log.e(TAG, "사진 저장 실패", exception)
                }
            }
        )
    }

    /**
     * 앱 내부 저장소에 파일 생성
     */
    private fun createAppPhotoFile(): File {
        val picturesDir = File(filesDir, "Pictures").apply {
            if (!exists()) mkdirs()
        }
        return File(picturesDir, generateFileName())
    }

    /**
     * 갤러리(공용 저장소)에 저장
     */
    private fun saveToGallery(sourceFile: File) {
        try {
            val displayName = generateFileName()

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())

                // Android 10 (Q) 이상
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Stampy")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val contentResolver = contentResolver
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    sourceFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                // Android 10 이상: IS_PENDING을 0으로 설정하여 완료 표시
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                }

                Log.d(TAG, "갤러리 저장 성공: $uri")
            }
        } catch (e: Exception) {
            Log.e(TAG, "갤러리 저장 실패", e)
        }
    }

    /**
     * 카메라 권한 확인
     */
    private fun allCameraPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    /**
     * 갤러리 권한 확인
     */
    private fun allGalleryPermissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 갤러리 권한 요청
     */
    private fun requestGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(permission),
            GALLERY_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_CODE -> {
                if (allCameraPermissionsGranted()) {
                    startCamera()
                } else {
                    showToast("카메라 권한이 필요합니다")
                    finish()
                }
            }
            GALLERY_PERMISSION_CODE -> {
                if (allGalleryPermissionsGranted()) {
                    switchToGalleryMode()
                } else {
                    showToast("갤러리 권한이 필요합니다")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

/**
 * 그리드 아이템 간격 조정 ItemDecoration (Camera용)
 */
class CameraGridSpacingItemDecoration(
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