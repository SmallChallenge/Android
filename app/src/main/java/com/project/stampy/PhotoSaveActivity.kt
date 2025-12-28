package com.project.stampy

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.project.stampy.ui.components.TagView
import com.project.stampy.utils.showToast

class PhotoSaveActivity : AppCompatActivity() {

    // 상단바
    private lateinit var btnBackTouchArea: FrameLayout
    private var btnComplete: MaterialButton? = null

    // 사진
    private lateinit var ivPhoto: ImageView
    private lateinit var tvTemplateOverlay: TextView

    // 카테고리
    private lateinit var categoryStudy: LinearLayout
    private lateinit var categoryStudyFrame: FrameLayout
    private lateinit var categoryExercise: LinearLayout
    private lateinit var categoryExerciseFrame: FrameLayout
    private lateinit var categoryFood: LinearLayout
    private lateinit var categoryFoodFrame: FrameLayout
    private lateinit var categoryEtc: LinearLayout
    private lateinit var categoryEtcFrame: FrameLayout

    // 공개 여부 태그
    private lateinit var tagPublic: TagView
    private lateinit var tagPrivate: TagView

    private var selectedCategory: String? = null
    private var isPublic = true  // 기본값: 전체 공개

    private var photoUri: Uri? = null
    private var templateName: String? = null

    companion object {
        const val EXTRA_PHOTO_URI = "extra_photo_uri"
        const val EXTRA_TEMPLATE_NAME = "extra_template_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_save)

        // Intent로 전달받은 데이터
        photoUri = intent.getParcelableExtra(EXTRA_PHOTO_URI)
        templateName = intent.getStringExtra(EXTRA_TEMPLATE_NAME)

        initViews()
        setupListeners()
        loadPhoto()
    }

    private fun initViews() {
        // 상단바
        btnBackTouchArea = findViewById(R.id.btn_back_touch_area)

        // 완료 버튼 - include의 id로 직접 찾기
        btnComplete = findViewById(R.id.btn_complete)
        btnComplete?.text = "완료"

        // 사진
        ivPhoto = findViewById(R.id.iv_photo)
        tvTemplateOverlay = findViewById(R.id.tv_template_overlay)

        // 카테고리 LinearLayout
        categoryStudy = findViewById(R.id.category_study)
        categoryExercise = findViewById(R.id.category_exercise)
        categoryFood = findViewById(R.id.category_food)
        categoryEtc = findViewById(R.id.category_etc)

        // 카테고리 FrameLayout (각각 id로 직접 찾기)
        categoryStudyFrame = findViewById(R.id.frame_study)
        categoryExerciseFrame = findViewById(R.id.frame_exercise)
        categoryFoodFrame = findViewById(R.id.frame_food)
        categoryEtcFrame = findViewById(R.id.frame_etc)

        // 공개 여부 태그
        tagPublic = findViewById(R.id.tag_public)
        tagPrivate = findViewById(R.id.tag_private)

        // 기본값: 전체 공개 선택
        tagPublic.isSelected = true
        tagPrivate.isSelected = false
    }

    private fun setupListeners() {
        // 뒤로가기
        btnBackTouchArea.setOnClickListener {
            finish()
        }

        // 완료 버튼
        btnComplete?.setOnClickListener {
            if (selectedCategory == null) {
                showToast("카테고리를 선택해주세요")
                return@setOnClickListener
            }

            // TODO: 사진 저장 로직
            showToast("사진이 저장되었습니다")
            finish()
        }

        // 카테고리 선택
        categoryStudy.setOnClickListener { selectCategory("공부") }
        categoryExercise.setOnClickListener { selectCategory("운동") }
        categoryFood.setOnClickListener { selectCategory("음식") }
        categoryEtc.setOnClickListener { selectCategory("기타") }

        // 공개 여부 태그
        tagPublic.setOnClickListener { selectPrivacy(true) }
        tagPrivate.setOnClickListener { selectPrivacy(false) }
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

        // 템플릿 오버레이
        templateName?.let { name ->
            tvTemplateOverlay.text = name
            tvTemplateOverlay.visibility = View.VISIBLE
        }
    }

    /**
     * 카테고리 선택
     */
    private fun selectCategory(category: String) {
        selectedCategory = category

        // 모든 카테고리를 40% opacity로 변경
        categoryStudyFrame.setBackgroundResource(R.drawable.bg_category_circle_unselected)
        categoryExerciseFrame.setBackgroundResource(R.drawable.bg_category_circle_unselected)
        categoryFoodFrame.setBackgroundResource(R.drawable.bg_category_circle_unselected)
        categoryEtcFrame.setBackgroundResource(R.drawable.bg_category_circle_unselected)

        // 선택된 카테고리만 100% opacity로 변경
        when (category) {
            "공부" -> categoryStudyFrame.setBackgroundResource(R.drawable.bg_category_circle_selected)
            "운동" -> categoryExerciseFrame.setBackgroundResource(R.drawable.bg_category_circle_selected)
            "음식" -> categoryFoodFrame.setBackgroundResource(R.drawable.bg_category_circle_selected)
            "기타" -> categoryEtcFrame.setBackgroundResource(R.drawable.bg_category_circle_selected)
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
}