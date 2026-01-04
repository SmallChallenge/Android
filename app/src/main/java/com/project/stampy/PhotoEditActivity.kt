package com.project.stampy

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.project.stampy.template.Template
import com.project.stampy.template.TemplateCategory
import com.project.stampy.etc.DoubleButtonDialog
import com.project.stampy.template.TemplateAdapter
import com.project.stampy.template.TemplateManager
import com.project.stampy.template.TemplateView
import com.project.stampy.utils.showToast

class PhotoEditActivity : AppCompatActivity() {

    // 상단바
    private lateinit var btnBackTouchArea: FrameLayout
    private var btnNext: MaterialButton? = null

    // 사진
    private lateinit var ivPhoto: ImageView
    private lateinit var photoContainer: FrameLayout
    private lateinit var templateView: TemplateView

    // 카테고리
    private lateinit var btnCategoryBasic: TextView
    private lateinit var btnCategoryMoody: TextView
    private lateinit var btnCategoryActive: TextView
    private lateinit var btnCategoryDigital: TextView

    // 로고 토글
    private lateinit var switchLogo: SwitchCompat

    // 템플릿 RecyclerView
    private lateinit var templateRecyclerView: RecyclerView
    private lateinit var templateAdapter: TemplateAdapter

    private var selectedCategory = TemplateCategory.BASIC
    private var selectedTemplate: Template? = null
    private var showLogo = true
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
        setupTemplateRecyclerView()

        // 기본 카테고리 선택
        selectCategory(TemplateCategory.BASIC, btnCategoryBasic)
    }

    private fun initViews() {
        // 상단바
        btnBackTouchArea = findViewById(R.id.btn_back_touch_area)
        btnNext = findViewById(R.id.btn_next)
        Log.d(TAG, "btnNext found: $btnNext")

        // 사진 컨테이너
        photoContainer = findViewById(R.id.photo_container)
        ivPhoto = findViewById(R.id.iv_photo)

        // 템플릿 뷰 추가
        templateView = TemplateView(this)
        photoContainer.addView(templateView)

        // 카테고리
        btnCategoryBasic = findViewById(R.id.btn_category_Basic)
        btnCategoryMoody = findViewById(R.id.btn_category_Moody)
        btnCategoryActive = findViewById(R.id.btn_category_Active)
        btnCategoryDigital = findViewById(R.id.btn_category_Digital)

        // 로고 토글
        switchLogo = findViewById(R.id.switch_logo)

        // 템플릿 RecyclerView
        templateRecyclerView = findViewById(R.id.template_recycler_view)
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
            navigateToPhotoSave()
        }

        // 카테고리 선택
        btnCategoryBasic.setOnClickListener {
            selectCategory(TemplateCategory.BASIC, btnCategoryBasic)
        }
        btnCategoryMoody.setOnClickListener {
            selectCategory(TemplateCategory.MOODY, btnCategoryMoody)
        }
        btnCategoryActive.setOnClickListener {
            selectCategory(TemplateCategory.ACTIVE, btnCategoryActive)
        }
        btnCategoryDigital.setOnClickListener {
            selectCategory(TemplateCategory.DIGITAL, btnCategoryDigital)
        }

        // 로고 토글
        switchLogo.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                // OFF로 변경 시 모달 표시
                showLogoOffDialog()
            } else {
                // ON으로 변경
                showLogo = true
                templateView.setLogoVisibility(true)
            }
        }
    }

    /**
     * 템플릿 RecyclerView 설정
     */
    private fun setupTemplateRecyclerView() {
        templateAdapter = TemplateAdapter { template ->
            onTemplateSelected(template)
        }

        templateRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                this@PhotoEditActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = templateAdapter

            // 동시에 눌리는 느낌 제거
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        }
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

            // TemplateView의 iv_photo에도 로드 (Moody 3용)
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
                        Log.d(TAG, "Template photo loaded: ${resource.width}x${resource.height}")
                    }

                    override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                        // Do nothing
                    }
                })

        } ?: Log.e(TAG, "photoUri is null, cannot load photo")
    }

    /**
     * 카테고리 선택
     */
    private fun selectCategory(category: TemplateCategory, button: TextView) {
        selectedCategory = category

        // 모든 버튼 초기화
        resetCategoryButton(btnCategoryBasic)
        resetCategoryButton(btnCategoryMoody)
        resetCategoryButton(btnCategoryActive)
        resetCategoryButton(btnCategoryDigital)

        // 선택된 버튼 활성화
        activateCategoryButton(button)

        // 해당 카테고리의 템플릿 로드
        loadTemplatesForCategory(category)
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
     * 카테고리별 템플릿 로드
     */
    private fun loadTemplatesForCategory(category: TemplateCategory) {
        val templates = TemplateManager.getTemplatesByCategory(category)

        templateAdapter.setSelectedPosition(0)
        templateAdapter.submitList(templates) {
            if (templates.isNotEmpty()) {
                onTemplateSelected(templates[0])
            }
        }
    }


    /**
     * 템플릿 선택
     */
    private fun onTemplateSelected(template: Template) {
        selectedTemplate = template
        templateView.applyTemplate(template, showLogo)

        Log.d(TAG, "템플릿 선택: ${template.name}")
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
                showLogo = true
            }
            .setOnConfirmListener {
                // 광고 시청 (TODO: 광고 연결)
                showToast("광고 기능은 곧 추가될 예정이에요.")
                // 광고 성공 후 OFF 유지
                showLogo = false
                switchLogo.isChecked = false
                templateView.setLogoVisibility(false)
            }
            .show()
    }

    /**
     * PhotoSaveActivity로 이동
     */
    private fun navigateToPhotoSave() {
        try {
            val intent = Intent(this, PhotoSaveActivity::class.java)
            intent.putExtra(PhotoSaveActivity.EXTRA_PHOTO_URI, photoUri)

            // 선택된 템플릿 정보 전달
            selectedTemplate?.let { template ->
                intent.putExtra(PhotoSaveActivity.EXTRA_TEMPLATE_NAME, template.name)
                intent.putExtra(PhotoSaveActivity.EXTRA_TEMPLATE_ID, template.id)  // 추가
                Log.d(TAG, "Template ID: ${template.id}, name: ${template.name}")
            }

            Log.d(TAG, "Starting PhotoSaveActivity...")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting PhotoSaveActivity: ${e.message}")
            e.printStackTrace()
            showToast("요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.")
        }
    }
}