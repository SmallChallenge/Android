package com.project.stampy

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    // 촬영 버튼
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

    private val CAMERA_PERMISSION_CODE = 101

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

        cameraExecutor = Executors.newSingleThreadExecutor()

        // 카메라 권한 확인
        if (allPermissionsGranted()) {
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

        // 촬영 버튼
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

        // 갤러리 버튼 (TODO)
        btnGallery.setOnClickListener {
            showToast("갤러리 기능은 곧 추가됩니다")
        }

        // 카메라 버튼 (이미 활성화 상태)
        btnCamera.setOnClickListener {
            // 이미 카메라 화면
        }
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

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                showToast("카메라 권한이 필요합니다")
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}