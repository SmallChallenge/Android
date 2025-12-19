package com.project.stampy

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.AuthRepository
import kotlinx.coroutines.launch
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnKakaoLogin: FrameLayout
    private lateinit var btnGoogleLogin: FrameLayout

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_GOOGLE_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 앱 서명 정보 출력 (디버깅용)
        printAppSignature()

        // TokenManager 및 Repository 초기화
        tokenManager = TokenManager(this)
        RetrofitClient.initialize(tokenManager)
        authRepository = AuthRepository(tokenManager)

        // Google Sign-In 설정
        setupGoogleSignIn()

        initViews()
        setupListeners()
    }

    /**
     * 앱의 SHA-1 서명 출력 (디버깅용)
     */
    private fun printAppSignature() {
        try {
            val packageInfo: PackageInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in packageInfo.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val sha1 = Base64.encodeToString(md.digest(), Base64.NO_WRAP)
                Log.d(TAG, "========================================")
                Log.d(TAG, "Package Name: $packageName")
                Log.d(TAG, "SHA-1 (Base64): $sha1")

                // SHA-1을 HEX 형식으로도 출력
                val hexSha1 = md.digest().joinToString(":") { "%02X".format(it) }
                Log.d(TAG, "SHA-1 (HEX): $hexSha1")
                Log.d(TAG, "========================================")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app signature", e)
        }
    }

    private fun setupGoogleSignIn() {
        try {
            // Web 클라이언트 ID 사용 (requestIdToken을 위해 필요)
            val webClientId = getString(R.string.google_web_client_id)
            Log.d(TAG, "========================================")
            Log.d(TAG, "Google Web Client ID from strings.xml:")
            Log.d(TAG, webClientId)
            Log.d(TAG, "Client ID length: ${webClientId.length}")
            Log.d(TAG, "Contains '.apps.googleusercontent.com': ${webClientId.contains(".apps.googleusercontent.com")}")
            Log.d(TAG, "========================================")

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)  // Web 클라이언트 ID 사용!
                .requestEmail()
                .requestProfile()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
            Log.d(TAG, "Google Sign-In client initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Google Sign-In", e)
            Toast.makeText(this, "구글 로그인 초기화 실패: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        btnKakaoLogin = findViewById(R.id.btn_kakao_login)
        btnGoogleLogin = findViewById(R.id.btn_google_login)
    }

    private fun setupListeners() {
        // 뒤로가기
        btnBack.setOnClickListener {
            finish()
        }

        // 카카오 로그인
        btnKakaoLogin.setOnClickListener {
            Toast.makeText(this, "카카오 로그인 준비중", Toast.LENGTH_SHORT).show()
        }

        // 구글 로그인
        btnGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        Log.d(TAG, "Starting Google Sign-In")
        try {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start sign in", e)
            Toast.makeText(this, "로그인 시작 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
    }

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
                performSocialLogin("GOOGLE", idToken)
            } else {
                Log.e(TAG, "========================================")
                Log.e(TAG, "ID Token is NULL!")
                Log.e(TAG, "Web 클라이언트 ID가 올바르게 설정되었는지 확인하세요!")
                Log.e(TAG, "========================================")

                Toast.makeText(
                    this,
                    "구글 로그인 실패: ID 토큰을 받지 못했습니다.\n" +
                            "Web 클라이언트 ID를 확인하세요.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: ApiException) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "Google Sign-In failed!")
            Log.e(TAG, "Status Code: ${e.statusCode}")
            Log.e(TAG, "Status Message: ${e.statusMessage}")
            Log.e(TAG, "========================================")

            val errorMessage = when (e.statusCode) {
                10 -> "개발자 오류 (코드 10): Google Cloud Console 설정을 확인하세요.\n" +
                        "- Android 클라이언트에 SHA-1 등록 확인\n" +
                        "- Web 클라이언트 ID 생성 및 사용 확인"
                12501 -> "사용자가 로그인을 취소했습니다"
                7 -> "네트워크 연결 오류"
                else -> "구글 로그인 실패 (코드: ${e.statusCode})\n${e.message}"
            }

            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during sign in", e)
            Toast.makeText(this, "로그인 중 오류 발생: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun performSocialLogin(socialType: String, accessToken: String) {
        Log.d(TAG, "Performing social login: $socialType")

        lifecycleScope.launch {
            try {
                val result = authRepository.socialLogin(socialType, accessToken)

                result.onSuccess { response ->
                    Log.d(TAG, "로그인 성공: ${response.username}, needNickname: ${response.needNickname}")

                    if (response.needNickname) {
                        Log.d(TAG, "Navigating to NicknameActivity")
                        navigateToNickname()
                    } else {
                        Log.d(TAG, "Navigating to MainActivity")
                        navigateToMain()
                    }
                }.onFailure { error ->
                    Log.e(TAG, "로그인 실패: ${error.message}", error)
                    Toast.makeText(
                        this@LoginActivity,
                        "서버 로그인 실패: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "로그인 오류", e)
                Toast.makeText(
                    this@LoginActivity,
                    "로그인 중 오류가 발생했습니다: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun navigateToNickname() {
        val intent = Intent(this, NicknameActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}