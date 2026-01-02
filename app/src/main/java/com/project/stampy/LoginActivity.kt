package com.project.stampy

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.AuthRepository
import com.project.stampy.ui.dialog.SingleButtonDialog
import com.project.stampy.utils.showToast
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var btnKakaoLogin: FrameLayout
    private lateinit var btnGoogleLogin: FrameLayout
    private lateinit var btnCloseTouchArea: FrameLayout
    private lateinit var tvTerms: TextView

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository

    // 커뮤니티에서 왔는지 여부
    private var shouldReturnToCommunity = false

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_GOOGLE_SIGN_IN = 9001
        private const val REQUEST_CODE_NICKNAME = 1001
        private const val TERMS_URL = "https://placid-aurora-3ad.notion.site/2b54e8ebd8b080c1a8bdd9267b94dc3e?source=copy_link"

        // 커뮤니티에서 왔는지 확인하는 Extra
        const val EXTRA_RETURN_TO_COMMUNITY = "extra_return_to_community"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 커뮤니티에서 왔는지 확인
        shouldReturnToCommunity = intent.getBooleanExtra(EXTRA_RETURN_TO_COMMUNITY, false)
        Log.d(TAG, "shouldReturnToCommunity: $shouldReturnToCommunity")

        // TokenManager 및 Repository 초기화
        tokenManager = TokenManager(this)
        RetrofitClient.initialize(tokenManager)
        authRepository = AuthRepository(tokenManager)

        // Google Sign-In 설정
        setupGoogleSignIn()

        initViews()
        setupListeners()
        setupTermsText()
    }

    private fun setupGoogleSignIn() {
        try {
            val webClientId = getString(R.string.google_web_client_id)
            Log.d(TAG, "Google Client ID: $webClientId")

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .requestProfile()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
            Log.d(TAG, "Google Sign-In client initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Google Sign-In", e)
            showToast("구글 로그인 초기화 실패: ${e.message}", Toast.LENGTH_LONG)
        }
    }

    private fun initViews() {
        btnKakaoLogin = findViewById(R.id.btn_kakao_login)
        btnGoogleLogin = findViewById(R.id.btn_google_login)
        btnCloseTouchArea = findViewById(R.id.btn_close_touch_area)
        tvTerms = findViewById(R.id.tv_terms)

        // 닫기 아이콘 색상 변경 (흰색)
        val ivClose = findViewById<ImageView>(R.id.iv_close)
        ivClose.setColorFilter(
            ContextCompat.getColor(this, android.R.color.white),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }

    private fun setupListeners() {
        // 닫기 버튼 - 로그인 페이지 닫기
        btnCloseTouchArea.setOnClickListener {
            finish()
        }

        btnKakaoLogin.setOnClickListener { signInWithKakao() }
        btnGoogleLogin.setOnClickListener { signInWithGoogle() }
    }

    private fun setupTermsText() {
        val fullText = "로그인하시면 이용약관 및 동의에 자동으로 동의합니다"
        val termsText = "이용약관"

        val spannableString = SpannableString(fullText)
        val startIndex = fullText.indexOf(termsText)
        val endIndex = startIndex + termsText.length

        spannableString.setSpan(
            UnderlineSpan(),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openTermsInWebView()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = tvTerms.currentTextColor
            }
        }

        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        tvTerms.text = spannableString
        tvTerms.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun openTermsInWebView() {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.EXTRA_URL, TERMS_URL)
        startActivity(intent)
    }

    /**
     * 카카오 로그인
     */
    private fun signInWithKakao() {
        Log.d(TAG, "Starting Kakao Sign-In")

        // 카카오톡 설치 여부 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            // 카카오톡으로 로그인
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        Log.d(TAG, "사용자가 로그인을 취소했습니다")
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    Log.d(TAG, "카카오톡 로그인 실패, 카카오계정으로 로그인 시도")
                    loginWithKakaoAccount()
                } else if (token != null) {
                    Log.d(TAG, "카카오톡 로그인 성공: ${token.accessToken.take(20)}...")
                    handleKakaoLogin(token)
                }
            }
        } else {
            // 카카오톡 미설치: 카카오계정으로 로그인
            Log.d(TAG, "카카오톡 미설치, 카카오계정으로 로그인")
            loginWithKakaoAccount()
        }
    }

    /**
     * 카카오계정으로 로그인
     */
    private fun loginWithKakaoAccount() {
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정 로그인 실패", error)
                showLoginFailedDialog()
            } else if (token != null) {
                Log.d(TAG, "카카오계정 로그인 성공: ${token.accessToken.take(20)}...")
                handleKakaoLogin(token)
            }
        }
    }

    /**
     * 카카오 로그인 성공 처리
     */
    private fun handleKakaoLogin(token: OAuthToken) {
        performSocialLogin("KAKAO", token.accessToken)
    }

    /**
     * 구글 로그인
     */
    private fun signInWithGoogle() {
        Log.d(TAG, "Starting Google Sign-In")
        try {
            googleSignInClient.signOut().addOnCompleteListener {
                startActivityForResult(googleSignInClient.signInIntent, RC_GOOGLE_SIGN_IN)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start sign in", e)
            showToast("로그인 시작 실패: ${e.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        when (requestCode) {
            RC_GOOGLE_SIGN_IN -> {
                // 구글 로그인 결과 처리
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleGoogleSignInResult(task)
            }

            REQUEST_CODE_NICKNAME -> {
                when (resultCode) {
                    RESULT_OK -> {
                        // 닉네임 설정 완료
                        Log.d(TAG, "닉네임 설정 완료")
                        navigateToMain()
                    }
                    RESULT_CANCELED -> {
                        val fromClose = data?.getBooleanExtra("FROM_CLOSE", false) ?: false

                        if (fromClose) {
                            // 닫기 버튼: 로그인 플로우 전체 취소
                            Log.d(TAG, "닉네임 설정 취소 - LoginActivity 종료")
                            finish()
                        } else {
                            // 뒤로가기 버튼: LoginActivity 유지
                            Log.d(TAG, "닉네임 페이지에서 뒤로가기 - LoginActivity 유지")
                            // 아무것도 안 함 (LoginActivity 화면 유지)
                        }
                    }
                }
            }
        }
    }

    /**
     * 구글 로그인 결과 처리
     */
    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            Log.d(TAG, "Google Sign-In successful")
            Log.d(TAG, "Account: ${account?.email}")
            Log.d(TAG, "Display Name: ${account?.displayName}")
            Log.d(TAG, "ID: ${account?.id}")

            val idToken = account?.idToken

            if (idToken != null) {
                Log.d(TAG, "ID Token received: ${idToken.take(20)}...")
                // 서버에 소셜 로그인 요청
                performSocialLogin("GOOGLE", idToken)
            } else {
                Log.e(TAG, "ID Token is null!")
                Log.e(TAG, "Account object: $account")
                showLoginFailedDialog()
            }
        } catch (e: ApiException) {
            // 12501: 사용자가 로그인 취소 - 모달 표시 안 함
            if (e.statusCode == 12501) {
                Log.d(TAG, "User cancelled Google sign in")
                return
            }

            // 그 외 에러 - 모달 표시
            Log.e(TAG, "Google sign in failed with status code: ${e.statusCode}", e)
            showLoginFailedDialog()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign in", e)
            showLoginFailedDialog()
        }
    }

    /**
     * 소셜 로그인 API 호출
     *
     * 1. PENDING → 약관 동의
     * 2. ACTIVE + needNickname=true → 닉네임 설정 (약관 동의는 이미 완료됨)
     * 3. ACTIVE + needNickname=false → 메인 화면
     */
    private fun performSocialLogin(socialType: String, accessToken: String) {
        Log.d(TAG, "Performing social login: $socialType")

        lifecycleScope.launch {
            try {
                val result = authRepository.socialLogin(socialType, accessToken)

                result.onSuccess { response ->
                    Log.d(TAG, "로그인 성공: ${response.nickname}, status: ${response.userStatus}, needNickname: ${response.needNickname}")

                    when {
                        // 1. 상태가 PENDING이면 약관 동의가 최우선
                        response.userStatus == "PENDING" -> {
                            Log.d(TAG, "상태가 PENDING → 약관 동의 Bottom Sheet 표시")
                            showTermsBottomSheet()
                        }

                        // 2. 닉네임이 null이거나 비어있으면 닉네임 설정
                        //    (needNickname 플래그 대신 실제 닉네임 값으로 판단)
                        response.nickname.isNullOrEmpty() -> {
                            Log.d(TAG, "닉네임 없음 (nickname=${response.nickname}) → 닉네임 설정으로 이동")
                            navigateToNickname()
                        }

                        // 3. 상태가 ACTIVE이고 닉네임도 있다면 메인으로
                        else -> {
                            Log.d(TAG, "기존 가입자 (닉네임: ${response.nickname}) → MainActivity로 이동")
                            navigateToMain()
                        }
                    }
                }.onFailure { error ->
                    Log.e(TAG, "로그인 실패: ${error.message}", error)
                    showLoginFailedDialog()
                }
            } catch (e: Exception) {
                Log.e(TAG, "로그인 오류", e)
                showLoginFailedDialog()
            }
        }
    }

    /**
     * 약관 동의 Bottom Sheet 표시
     */
    private fun showTermsBottomSheet() {
        val bottomSheet = TermsBottomSheetDialog()
        bottomSheet.show(supportFragmentManager, "TermsBottomSheet")
    }

    /**
     * 로그인 실패 모달 표시
     */
    private fun showLoginFailedDialog() {
        SingleButtonDialog(this)
            .setTitle("로그인에 실패했어요.\n다시 시도해주세요.")
            .show()
    }

    /**
     * 닉네임 설정 화면으로 이동
     */
    private fun navigateToNickname() {
        val intent = Intent(this, NicknameActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_NICKNAME)
    }

    /**
     * 메인 화면으로 이동
     */
    private fun navigateToMain() {
        if (shouldReturnToCommunity) {
            // 커뮤니티에서 왔으면 커뮤니티 탭으로 이동
            Log.d(TAG, "커뮤니티 탭으로 이동")
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(MainActivity.EXTRA_NAVIGATE_TO_COMMUNITY, true)
            })
        } else {
            // 일반 로그인이면 내기록 탭으로 이동
            Log.d(TAG, "내기록 탭으로 이동")
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
        finish()
    }
}