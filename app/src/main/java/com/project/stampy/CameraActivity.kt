package com.project.stampy

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.project.stampy.utils.showToast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var captureButton: FloatingActionButton
    private lateinit var closeButton: FloatingActionButton

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val CAMERA_PERMISSION_CODE = 101

    companion object {
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

        previewView = findViewById(R.id.preview_view)
        captureButton = findViewById(R.id.btn_capture)
        closeButton = findViewById(R.id.btn_close)

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

        // 촬영 버튼
        captureButton.setOnClickListener {
            takePhoto()
        }

        // 닫기 버튼
        closeButton.setOnClickListener {
            finish()
        }
    }

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
                .build()

            // 후면 카메라 선택
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraActivity", "카메라 시작 실패", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

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
                    Log.d("CameraActivity", "앱 저장소 저장: ${appFile.absolutePath}")

                    // 2. 갤러리(공용 저장소)에도 저장
                    saveToGallery(appFile)
                    showToast("사진 저장 완료!")

                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    showToast("사진 저장 실패:  ${exception.message}")
                    Log.e("CameraActivity", "사진 저장 실패", exception)
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

                Log.d("CameraActivity", "갤러리 저장 성공: $uri")
            }
        } catch (e: Exception) {
            Log.e("CameraActivity", "갤러리 저장 실패", e)
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