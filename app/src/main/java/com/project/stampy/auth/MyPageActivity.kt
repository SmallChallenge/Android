package com.project.stampy.auth

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.button.MaterialButton
import com.project.stampy.MainActivity
import com.project.stampy.R
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.AuthRepository
import com.project.stampy.etc.DoubleButtonDialog
import com.project.stampy.etc.SingleButtonDialog
import com.project.stampy.etc.WebViewActivity
import com.project.stampy.utils.showToast
import kotlinx.coroutines.launch

class MyPageActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView

    // 프로필 라인
    private lateinit var tvNickname: TextView
    private lateinit var tvGuestRestriction: TextView

    // 로그인 유도 공간 (비회원만)
    private lateinit var layoutLoginPromotion: LinearLayout
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvPublicFeature: TextView

    // 설정 메뉴
    private lateinit var btnTerms: LinearLayout
    private lateinit var btnPrivacy: LinearLayout
    private lateinit var btnOpenSource: LinearLayout
    private lateinit var btnLogout: LinearLayout

    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository

    // 애드몹
    private lateinit var mAdView: AdView

    // 앰플리튜드
    private lateinit var amplitude: Amplitude

    companion object {
        private const val TAG = "MyPageActivity"
        private const val TERMS_URL = "https://sage-hare-ff7.notion.site/2d5f2907580d80df9a21f95acd343d3f?source=copy_link"
        private const val PRIVACY_URL = "https://sage-hare-ff7.notion.site/2d5f2907580d80eda745ccfbda543bc5?source=copy_link"
        private const val OPEN_SOURCE_URL = "https://sage-hare-ff7.notion.site/2d8f2907580d80e4accee06ce4da69cd"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        // TokenManager 및 Repository 초기화
        tokenManager = TokenManager(this)
        RetrofitClient.initialize(tokenManager)
        authRepository = AuthRepository(tokenManager)

        // Amplitude 초기화
        amplitude = Amplitude(
            Configuration(
                apiKey = getString(R.string.amplitude_api_key),
                context = applicationContext
            )
        )

        // 로그인 상태라면 사용자 식별 (이후 로그아웃 이벤트와 연결하기 위함)
        if (tokenManager.isLoggedIn()) {
            amplitude.setUserId("user_${tokenManager.getUserId()}")
        }

        // 애드몹 Mobile SDK 초기화
        MobileAds.initialize(this) {}
        // 뷰 초기화 및 광고 로드
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        initViews()
        setupListeners()
        setupPublicFeatureText()
        updateUI()
    }

    override fun onPause() {
        mAdView.pause()
        super.onPause()
    }

    override fun onDestroy() {
        mAdView.destroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        mAdView.resume()
        updateUI()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)

        // 프로필 라인
        tvNickname = findViewById(R.id.tv_nickname)
        tvGuestRestriction = findViewById(R.id.tv_guest_restriction)

        // 로그인 유도 공간
        layoutLoginPromotion = findViewById(R.id.layout_login_promotion)
        btnLogin = findViewById(R.id.btn_login)
        tvPublicFeature = findViewById(R.id.tv_public_feature)

        // 설정 메뉴
        btnTerms = findViewById(R.id.btn_terms)
        btnPrivacy = findViewById(R.id.btn_privacy)
        btnOpenSource = findViewById(R.id.btn_open_source)
        btnLogout = findViewById(R.id.btn_logout)
    }

    /**
     * "전체공개" 부분만 볼드 처리
     */
    private fun setupPublicFeatureText() {
        val fullText = "✔️ 기록 전체공개 설정 가능"
        val boldText = "전체공개"
        val spannableString = SpannableString(fullText)

        val startIndex = fullText.indexOf(boldText)
        if (startIndex >= 0) {
            spannableString.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                startIndex,
                startIndex + boldText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        tvPublicFeature.text = spannableString
    }

    private fun setupListeners() {
        // 뒤로가기
        btnBack.setOnClickListener {
            finish()
        }

        // 프로필 레이아웃 클릭 시 (로그인 상태면 내 정보로 이동)
        findViewById<LinearLayout>(R.id.layout_profile).setOnClickListener {
            if (tokenManager.isLoggedIn()) {
                val intent = Intent(this, MyInfoActivity::class.java)
                startActivity(intent)
            }
        }

        // 로그인 버튼
        btnLogin.setOnClickListener {
            navigateToLogin()
        }

        // 내 정보 버튼
        findViewById<LinearLayout>(R.id.layout_profile).setOnClickListener {
            if (tokenManager.isLoggedIn()) {
                val intent = Intent(this, MyInfoActivity::class.java)
                startActivity(intent)
            }
        }

        // 이용약관
        btnTerms.setOnClickListener {
            openWebView(TERMS_URL)
        }

        // 개인정보 처리방침
        btnPrivacy.setOnClickListener {
            openWebView(PRIVACY_URL)
        }

        // 오픈소스 라이센스
        btnOpenSource.setOnClickListener {
            openWebView(OPEN_SOURCE_URL)
        }

        // 로그아웃
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    /**
     * UI 업데이트 (로그인 상태에 따라)
     */
    private fun updateUI() {
        val isLoggedIn = tokenManager.isLoggedIn()
        val ivArrow = findViewById<ImageView>(R.id.iv_arrow_info)

        Log.d(TAG, "updateUI - isLoggedIn: $isLoggedIn")

        if (isLoggedIn) {
            // 로그인 상태
            val nickname = tokenManager.getNickname() ?: "사용자"
            tvNickname.text = nickname
            tvGuestRestriction.visibility = View.GONE  // 제한 텍스트 숨김

            // 로그인 유도 공간 숨김
            layoutLoginPromotion.visibility = View.GONE
            btnLogin.visibility = View.GONE

            // 로그아웃 버튼 표시
            btnLogout.visibility = View.VISIBLE

            // 프로필 화살표 버튼 표시
            ivArrow.visibility = View.VISIBLE

            Log.d(TAG, "UI updated to LOGGED IN - nickname: $nickname")
        } else {
            // 비로그인 상태 (게스트)
            tvNickname.text = "게스트"
            tvGuestRestriction.visibility = View.VISIBLE  // 제한 텍스트 표시

            // 로그인 유도 공간 표시
            layoutLoginPromotion.visibility = View.VISIBLE
            btnLogin.visibility = View.VISIBLE

            // 로그아웃 버튼 숨김
            btnLogout.visibility = View.GONE
            // 프로필 화살표 버튼 표시
            ivArrow.visibility = View.GONE

            Log.d(TAG, "UI updated to GUEST state")
        }
    }

    /**
     * 로그인 화면으로 이동
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    /**
     * WebView로 URL 열기
     */
    private fun openWebView(url: String) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.EXTRA_URL, url)
        startActivity(intent)
    }

    /**
     * 로그아웃 확인 다이얼로그
     */
    private fun showLogoutDialog() {
        DoubleButtonDialog(this)
            .setTitle("로그아웃하시겠습니까?")
            .setDescription("로그아웃 후 작성한 기록은 백업되지 않으며,\n비로그인 상태 기록은 최대 20개로 제한됩니다.")
            .setCancelButtonText("취소")
            .setConfirmButtonText("로그아웃")
            .setOnCancelListener {
                Log.d(TAG, "로그아웃 취소")
            }
            .setOnConfirmListener {
                performLogout()
            }
            .show()
    }

    /**
     * 로그아웃 실행
     */
    private fun performLogout() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "로그아웃 시작")

                val result = authRepository.logout(allDevices = false)

                result.onSuccess {
                    Log.d(TAG, "로그아웃 성공")

                    // 1. 로그아웃 성공 이벤트 전송 (명시적 액션)
                    amplitude.track("logout_success", mapOf(
                        "platform" to "android",
                        "user_id" to tokenManager.getUserId() // 마지막 기록용
                    ))

                    // 2. Amplitude 사용자 식별 해제 (이후 데이터는 익명 수집)
                    amplitude.setUserId(null)

                    // 토스트 메시지 표시
                    showToast("로그아웃이 완료되었어요.")

                    // 내 기록 화면으로 이동
                    navigateToMyRecords()

                }.onFailure { error ->
                    Log.e(TAG, "로그아웃 실패: ${error.message}")

                    // 실패 시 모달 표시
                    showLogoutErrorDialog()
                }
            } catch (e: Exception) {
                Log.e(TAG, "로그아웃 오류", e)
                showLogoutErrorDialog()
            }
        }
    }

    /**
     * 로그아웃 실패 모달
     */
    private fun showLogoutErrorDialog() {
        SingleButtonDialog(this)
            .setTitle("로그아웃에 실패했어요.\n다시 시도해주세요.")
            .show()
    }

    /**
     * 내 기록 화면으로 이동
     */
    private fun navigateToMyRecords() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra(MainActivity.EXTRA_NAVIGATE_TO_STORAGE, true)
        startActivity(intent)
        finish()
    }
}