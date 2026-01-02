package com.project.stampy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.project.stampy.etc.DoubleButtonDialog
import com.project.stampy.utils.showToast

class PhotoEditActivity : AppCompatActivity() {

    // 상단바
    private lateinit var btnBackTouchArea: FrameLayout
    private var btnNext: MaterialButton? = null

    // 사진
    private lateinit var ivPhoto: ImageView
    private lateinit var tvTemplateOverlay: TextView

    // 카테고리
    private lateinit var btnCategoryBasic: TextView
    private lateinit var btnCategoryMoody: TextView
    private lateinit var btnCategoryActive: TextView
    private lateinit var btnCategoryDigital: TextView

    // 로고 토글
    private lateinit var switchLogo: SwitchCompat

    // 템플릿
    private lateinit var template1: FrameLayout
    private lateinit var template2: FrameLayout
    private lateinit var template3: FrameLayout
    private lateinit var template4: FrameLayout
    private lateinit var template5: FrameLayout

    private var selectedTemplate: FrameLayout? = null

    private var selectedCategory = "Basic"
    private var photoUri: Uri? = null

    companion object {
        const val TAG = "PhotoEditActivity"
        const val EXTRA_PHOTO_URI = "extra_photo_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_edit)

        // Intent로 전달받은 사진 URI
        photoUri = intent.getParcelableExtra(EXTRA_PHOTO_URI)
        Log.d(TAG, "onCreate - photoUri: $photoUri")

        initViews()
        setupListeners()
        loadPhoto()

        activateCategoryButton(btnCategoryBasic)    // 기본 선택
    }

    private fun initViews() {
        // 상단바
        btnBackTouchArea = findViewById(R.id.btn_back_touch_area)

        // 다음 버튼 - include의 id로 직접 찾기
        btnNext = findViewById(R.id.btn_next)
        Log.d(TAG, "btnNext found: $btnNext")

        // 사진
        ivPhoto = findViewById(R.id.iv_photo)
        tvTemplateOverlay = findViewById(R.id.tv_template_overlay)

        // 카테고리
        btnCategoryBasic = findViewById(R.id.btn_category_Basic)
        btnCategoryMoody = findViewById(R.id.btn_category_Moody)
        btnCategoryActive = findViewById(R.id.btn_category_Active)
        btnCategoryDigital = findViewById(R.id.btn_category_Digital)

        // 로고 토글
        switchLogo = findViewById(R.id.switch_logo)

        // 템플릿
        template1 = findViewById(R.id.template_1)
        template2 = findViewById(R.id.template_2)
        template3 = findViewById(R.id.template_3)
        template4 = findViewById(R.id.template_4)
        template5 = findViewById(R.id.template_5)
    }

    private fun setupListeners() {
        // 뒤로가기
        btnBackTouchArea.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            finish()
        }

        // 다음 버튼
        btnNext?.setOnClickListener {
            Log.d(TAG, "다음 버튼 클릭됨!")
            Log.d(TAG, "photoUri: $photoUri")
            Log.d(TAG, "template overlay visible: ${tvTemplateOverlay.visibility == View.VISIBLE}")

            try {
                // 사진 저장 화면으로 이동
                val intent = Intent(this, PhotoSaveActivity::class.java)
                intent.putExtra(PhotoSaveActivity.EXTRA_PHOTO_URI, photoUri)

                // 선택된 템플릿 정보 전달
                if (tvTemplateOverlay.visibility == View.VISIBLE) {
                    intent.putExtra(PhotoSaveActivity.EXTRA_TEMPLATE_NAME, tvTemplateOverlay.text.toString())
                    Log.d(TAG, "Template name: ${tvTemplateOverlay.text}")
                }

                Log.d(TAG, "Starting PhotoSaveActivity...")
                startActivity(intent)
                Log.d(TAG, "PhotoSaveActivity started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting PhotoSaveActivity: ${e.message}")
                e.printStackTrace()
                showToast("화면 전환 오류: ${e.message}")
            }
        } ?: Log.e(TAG, "btnNext is null!")

        // 카테고리 선택
        btnCategoryBasic.setOnClickListener { selectCategory("Basic", btnCategoryBasic) }
        btnCategoryMoody.setOnClickListener { selectCategory("Moody", btnCategoryMoody) }
        btnCategoryActive.setOnClickListener { selectCategory("Active", btnCategoryActive) }
        btnCategoryDigital.setOnClickListener { selectCategory("Digital", btnCategoryDigital) }

        // 로고 토글
        switchLogo.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                // OFF로 변경 시 모달 표시
                showLogoOffDialog()
            }
        }

        // 템플릿 선택
        template1.setOnClickListener { selectTemplate(template1, "템플릿 1") }
        template2.setOnClickListener { selectTemplate(template2, "템플릿 2") }
        template3.setOnClickListener { selectTemplate(template3, "템플릿 3") }
        template4.setOnClickListener { selectTemplate(template4, "템플릿 4") }
        template5.setOnClickListener { selectTemplate(template5, "템플릿 5") }
    }

    /**
     * 사진 로드
     */
    private fun loadPhoto() {
        photoUri?.let { uri ->
            Log.d(TAG, "Loading photo: $uri")
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(ivPhoto)
        } ?: Log.e(TAG, "photoUri is null, cannot load photo")
    }

    /**
     * 카테고리 선택
     */
    private fun selectCategory(category: String, button: TextView) {
        selectedCategory = category

        // 모든 버튼 초기화
        resetCategoryButton(btnCategoryBasic)
        resetCategoryButton(btnCategoryMoody)
        resetCategoryButton(btnCategoryActive)
        resetCategoryButton(btnCategoryDigital)

        // 선택된 버튼 활성화
        activateCategoryButton(button)
    }

    private fun resetCategoryButton(button: TextView) {
        button.setTextColor(getColor(R.color.gray_400))
        button.setBackgroundResource(0)
    }

    private fun activateCategoryButton(button: TextView) {
        button.setTextColor(getColor(R.color.gray_50))
        button.setBackgroundResource(R.drawable.bg_category_selected)
    }

    /**
     * 템플릿 선택
     */
    private fun selectTemplate(template: FrameLayout, templateName: String) {
        // 이전 선택 해제
        selectedTemplate?.setBackgroundResource(R.drawable.bg_template_item)

        // 새로운 템플릿 선택
        selectedTemplate = template
        template.setBackgroundResource(R.drawable.bg_template_item_selected)

        // 템플릿 적용
        applyTemplate(templateName)
    }

    /**
     * 로고 OFF 모달 표시
     */
    private fun showLogoOffDialog() {
        DoubleButtonDialog(this)
            .setTitle("광고 시청 후\n워터마크를 제거하세요.")
            .setCancelButtonText("취소")
            .setConfirmButtonText("광고 시청")
            .setOnCancelListener {
                // 취소 - 토글 다시 ON
                switchLogo.isChecked = true
            }
            .setOnConfirmListener {
                // 광고 시청 (TODO: 광고 연결)
                showToast("광고 기능은 곧 추가됩니다")
                // 광고 성공 후 OFF 유지
                switchLogo.isChecked = false
            }
            .show()
    }

    /**
     * 템플릿 적용
     */
    private fun applyTemplate(templateName: String) {
        tvTemplateOverlay.text = templateName
        tvTemplateOverlay.visibility = View.VISIBLE
    }
}