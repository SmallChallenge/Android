package com.project.stampy

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.project.stampy.ui.dialog.DoubleButtonDialog
import com.project.stampy.utils.showToast

class PhotoEditActivity : AppCompatActivity() {

    // 상단바
    private lateinit var btnBackTouchArea: FrameLayout
    private var btnNext: com.google.android.material.button.MaterialButton? = null

    // 사진
    private lateinit var ivPhoto: ImageView
    private lateinit var tvTemplateOverlay: TextView

    // 카테고리
    private lateinit var btnCategoryAll: TextView
    private lateinit var btnCategoryStudy: TextView
    private lateinit var btnCategoryExercise: TextView
    private lateinit var btnCategoryFood: TextView
    private lateinit var btnCategoryEtc: TextView

    // 로고 토글
    private lateinit var switchLogo: SwitchCompat

    // 템플릿
    private lateinit var template1: FrameLayout
    private lateinit var template2: FrameLayout
    private lateinit var template3: FrameLayout
    private lateinit var template4: FrameLayout
    private lateinit var template5: FrameLayout

    private var selectedTemplate: FrameLayout? = null

    private var selectedCategory = "전체"
    private var photoUri: Uri? = null

    companion object {
        const val EXTRA_PHOTO_URI = "extra_photo_uri"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_edit)

        // Intent로 전달받은 사진 URI
        photoUri = intent.getParcelableExtra(EXTRA_PHOTO_URI)

        initViews()
        setupListeners()
        loadPhoto()
    }

    private fun initViews() {
        // 상단바
        btnBackTouchArea = findViewById(R.id.btn_back_touch_area)

        // 다음 버튼 (include로 추가된 버튼)
        val btnNextView = findViewById<View>(R.id.btn_next)
        btnNext = btnNextView.findViewById(R.id.btn_small_primary)

        // 사진
        ivPhoto = findViewById(R.id.iv_photo)
        tvTemplateOverlay = findViewById(R.id.tv_template_overlay)

        // 카테고리
        btnCategoryAll = findViewById(R.id.btn_category_all)
        btnCategoryStudy = findViewById(R.id.btn_category_study)
        btnCategoryExercise = findViewById(R.id.btn_category_exercise)
        btnCategoryFood = findViewById(R.id.btn_category_food)
        btnCategoryEtc = findViewById(R.id.btn_category_etc)

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
            finish()
        }

        // 다음 버튼
        btnNext?.setOnClickListener {
            // TODO: 다음 화면으로 이동
            showToast("다음 화면으로 이동")
        }

        // 카테고리 선택
        btnCategoryAll.setOnClickListener { selectCategory("전체", btnCategoryAll) }
        btnCategoryStudy.setOnClickListener { selectCategory("공부", btnCategoryStudy) }
        btnCategoryExercise.setOnClickListener { selectCategory("운동", btnCategoryExercise) }
        btnCategoryFood.setOnClickListener { selectCategory("음식", btnCategoryFood) }
        btnCategoryEtc.setOnClickListener { selectCategory("기타", btnCategoryEtc) }

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
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(ivPhoto)
        }
    }

    /**
     * 카테고리 선택
     */
    private fun selectCategory(category: String, button: TextView) {
        selectedCategory = category

        // 모든 버튼 초기화
        resetCategoryButton(btnCategoryAll)
        resetCategoryButton(btnCategoryStudy)
        resetCategoryButton(btnCategoryExercise)
        resetCategoryButton(btnCategoryFood)
        resetCategoryButton(btnCategoryEtc)

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