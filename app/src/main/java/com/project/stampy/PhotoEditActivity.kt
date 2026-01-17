package com.project.stampy

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.AdRequest
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.project.stampy.data.local.TokenManager

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

    // 애드몹
    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false // 중복 로딩 방지
    private var isRewardEarned = false // 광고 시청 완료 여부 저장

    // 템플릿 RecyclerView
    private lateinit var templateRecyclerView: RecyclerView
    private lateinit var templateAdapter: TemplateAdapter

    private var selectedCategory = TemplateCategory.BASIC
    private var selectedTemplate: Template? = null
    private var showLogo = true
    private var photoUri: Uri? = null
    private var photoTakenAt: Long = 0L

    private lateinit var amplitude: Amplitude
    private lateinit var tokenManager: TokenManager

    // 실시간 시간 업데이트용
    private val timeUpdateHandler = Handler(Looper.getMainLooper())
    private var timeUpdateRunnable: Runnable? = null

    companion object {
        const val TAG = "PhotoEditActivity"
        const val EXTRA_PHOTO_URI = "extra_photo_uri"
        const val EXTRA_PHOTO_TAKEN_AT = "extra_photo_taken_at" // 갤러리 사진의 촬영 시간
        private const val TIME_UPDATE_INTERVAL = 1000L // 1초마다 체크
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_edit)

        // Intent로 전달받은 사진 URI
        photoUri = intent.getParcelableExtra(EXTRA_PHOTO_URI)
        photoTakenAt = intent.getLongExtra(EXTRA_PHOTO_TAKEN_AT, System.currentTimeMillis())

        Log.d(TAG, "onCreate - photoUri: $photoUri, takenAt: $photoTakenAt")

        initViews()
        setupListeners()
        loadPhoto()
        setupTemplateRecyclerView()
        loadRewardedAd()

        // 촬영 시간 설정
        templateView.setPhotoTakenAt(photoTakenAt)

        // 기본 템플릿 적용 (Basic 1)
        val basicTemplate = TemplateManager.getTemplatesByCategory(TemplateCategory.BASIC).firstOrNull()
        basicTemplate?.let {
            selectedTemplate = it
            templateView.applyTemplate(it, showLogo = true)
            Log.d(TAG, "기본 템플릿 적용: ${it.name}")
        }

        // 기본 카테고리 선택 (이건 UI만 변경, 템플릿은 위에서 이미 적용됨)
        selectCategory(TemplateCategory.BASIC, btnCategoryBasic)

        tokenManager = TokenManager(this)
        amplitude = Amplitude(Configuration(getString(R.string.amplitude_api_key), applicationContext))

        // 유저 식별
        if (tokenManager.isLoggedIn()) {
            val savedUserId = tokenManager.getUserId()
            if (savedUserId != -1L) {
                amplitude.setUserId("user_$savedUserId")
            }
        }

        // 실시간 시간 업데이트 시작
        startTimeUpdate()
    }

    override fun onResume() {
        super.onResume()
        // 화면 복귀 시 시간 업데이트 재시작
        startTimeUpdate()
    }

    override fun onPause() {
        super.onPause()
        // 화면 벗어날 때 시간 업데이트 중지
        stopTimeUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 액티비티 종료 시 시간 업데이트 중지
        stopTimeUpdate()
    }

    /**
     * 실시간 시간 업데이트 시작
     */
    private fun startTimeUpdate() {
        // 기존 Runnable이 있으면 제거
        stopTimeUpdate()

        var lastMinute = -1 // 마지막으로 업데이트한 분

        timeUpdateRunnable = object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val calendar = java.util.Calendar.getInstance().apply {
                    timeInMillis = currentTime
                }
                val currentMinute = calendar.get(java.util.Calendar.MINUTE)

                // 분이 바뀌었을 때만 템플릿 업데이트
                if (currentMinute != lastMinute) {
                    lastMinute = currentMinute
                    photoTakenAt = currentTime
                    templateView.setPhotoTakenAt(currentTime)
                    Log.d(TAG, "시간 업데이트: ${calendar.get(java.util.Calendar.HOUR_OF_DAY)}:${currentMinute}")
                }

                // 1초 후 다시 실행
                timeUpdateHandler.postDelayed(this, TIME_UPDATE_INTERVAL)
            }
        }

        // 즉시 시작
        timeUpdateRunnable?.let { timeUpdateHandler.post(it) }
    }

    /**
     * 실시간 시간 업데이트 중지
     */
    private fun stopTimeUpdate() {
        timeUpdateRunnable?.let {
            timeUpdateHandler.removeCallbacks(it)
        }
        timeUpdateRunnable = null
    }

    // 애드몹 광고 불러오는 함수
    private fun loadRewardedAd() {
        if (isAdLoading || rewardedAd != null || isRewardEarned) return // 이미 보상을 얻었다면 로드 안 함
        isAdLoading = true

        val adRequest = AdRequest.Builder().build()

        // strings.xml에서 ID 가져오기
        val adUnitId = getString(R.string.admob_reward_unit_id)

        RewardedAd.load(this, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "보상형 광고 로드 실패: ${adError.message}")
                rewardedAd = null
                isAdLoading = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "보상형 광고 로드 완료")
                rewardedAd = ad
                isAdLoading = false
            }
        })
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

        switchLogo.setOnClickListener {
            val isChecked = switchLogo.isChecked

            if (!isChecked) { // 로고를 끄려고 할 때
                if (isRewardEarned) {
                    // 이미 광고를 봤다면 바로 끄기 가능
                    showLogo = false
                    templateView.setLogoVisibility(false)
                } else {
                    // 광고를 아직 안 봤다면 다이얼로그 띄우기
                    switchLogo.isChecked = true
                    showLogoOffDialog()
                }
            } else { // 로고를 다시 켤 때
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

        // 현재 선택된 템플릿이 새 카테고리에 속하는지 확인
        val selectedTemplateInCategory = templates.indexOfFirst { it.id == selectedTemplate?.id }

        if (selectedTemplateInCategory >= 0) {
            // 현재 선택된 템플릿이 새 카테고리에 있으면 선택 상태 유지
            templateAdapter.setSelectedPosition(selectedTemplateInCategory)
        } else {
            // 현재 선택된 템플릿이 새 카테고리에 없으면 선택 해제 (-1)
            templateAdapter.setSelectedPosition(-1)
        }

        templateAdapter.submitList(templates)
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
            }
            .setOnConfirmListener {
                showRewardAd()
            }
            .show()
    }

    private fun showRewardAd() {
        rewardedAd?.let { ad ->
            ad.show(this) { rewardItem ->
                // 보상 획득 성공 시 플래그 true로 변경
                isRewardEarned = true

                // 광고 시청 완료 이벤트 전송
                // amplitude.track("complete_reward_ad", mapOf(
                //     "is_logged_in" to tokenManager.isLoggedIn(),
                //    "platform" to "android",
                //    "ad_type" to "remove_watermark"
                //))

                showLogo = false
                switchLogo.isChecked = false
                templateView.setLogoVisibility(false)

                rewardedAd = null // 사용한 광고 객체 비우기
            }
        } ?: run {
            showToast("광고를 불러오는 중입니다. 잠시 후 다시 시도해주세요.")
            loadRewardedAd()
        }
    }

    /**
     * PhotoSaveActivity로 이동
     */
    private fun navigateToPhotoSave() {
        try {
            val intent = Intent(this, PhotoSaveActivity::class.java)
            intent.putExtra(PhotoSaveActivity.EXTRA_PHOTO_URI, photoUri)
            intent.putExtra(PhotoSaveActivity.EXTRA_PHOTO_TAKEN_AT, photoTakenAt)

            // 선택된 템플릿 정보 전달
            selectedTemplate?.let { template ->
                intent.putExtra(PhotoSaveActivity.EXTRA_TEMPLATE_NAME, template.name)
                intent.putExtra(PhotoSaveActivity.EXTRA_TEMPLATE_ID, template.id)
                Log.d(TAG, "Template ID: ${template.id}, name: ${template.name}")
            }

            // 로고 표시 여부 전달 추가
            intent.putExtra(PhotoSaveActivity.EXTRA_SHOW_LOGO, showLogo)
            Log.d(TAG, "Show logo: $showLogo, takenAt: $photoTakenAt")

            Log.d(TAG, "Starting PhotoSaveActivity...")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting PhotoSaveActivity: ${e.message}")
            e.printStackTrace()
            showToast("요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.")
        }
    }
}