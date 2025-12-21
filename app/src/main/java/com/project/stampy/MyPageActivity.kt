package com.project.stampy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.AuthRepository
import kotlinx.coroutines.launch

class MyPageActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView

    // 비로그인 상태 뷰
    private lateinit var layoutGuest: LinearLayout
    private lateinit var btnLogin: Button

    // 로그인 상태 뷰
    private lateinit var layoutLoggedIn: LinearLayout
    private lateinit var tvUserName: TextView
    private lateinit var btnLogout: TextView

    // 공통 메뉴
    private lateinit var tvAppVersion: TextView
    private lateinit var tvWithdrawal: TextView

    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository

    companion object {
        private const val TAG = "MyPageActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        // TokenManager 및 Repository 초기화
        tokenManager = TokenManager(this)
        RetrofitClient.initialize(tokenManager)
        authRepository = AuthRepository(tokenManager)

        initViews()
        setupListeners()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        // 화면이 다시 보일 때마다 UI 업데이트 (로그인 후 돌아왔을 때)
        updateUI()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)

        // 비로그인 상태 (XML에서 layout_logged_out)
        layoutGuest = findViewById(R.id.layout_logged_out)
        btnLogin = findViewById(R.id.btn_login_signup)

        // 로그인 상태 (XML에서 layout_logged_in)
        layoutLoggedIn = findViewById(R.id.layout_logged_in)
        tvUserName = findViewById(R.id.tv_user_name)
        btnLogout = findViewById(R.id.btn_logout)

        // 공통 메뉴
        tvAppVersion = findViewById(R.id.tv_app_version)
        tvWithdrawal = findViewById(R.id.btn_delete_account)
    }

    private fun setupListeners() {
        // 뒤로가기
        btnBack.setOnClickListener {
            finish()
        }

        // 로그인/회원가입 버튼
        btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // 로그아웃 버튼
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        // 탈퇴하기
        tvWithdrawal.setOnClickListener {
            if (authRepository.isLoggedIn()) {
                showWithdrawalDialog()
            } else {
                showToast("로그인이 필요합니다")
            }
        }
    }

    /**
     * UI 업데이트 (로그인 상태에 따라)
     */
    private fun updateUI() {
        val isLoggedIn = authRepository.isLoggedIn()

        Log.d(TAG, "updateUI called - isLoggedIn: $isLoggedIn")

        if (isLoggedIn) {
            // 로그인 상태
            layoutGuest.visibility = View.GONE
            layoutLoggedIn.visibility = View.VISIBLE
            btnLogout.visibility = View.VISIBLE

            // 사용자 이름 표시
            val nickname = tokenManager.getNickname() ?: "사용자"
            tvUserName.text = nickname

            Log.d(TAG, "UI updated to LOGGED IN state - nickname: $nickname")
        } else {
            // 비로그인 상태
            layoutGuest.visibility = View.VISIBLE
            layoutLoggedIn.visibility = View.GONE
            btnLogout.visibility = View.GONE

            Log.d(TAG, "UI updated to LOGGED OUT state")
        }

        // 강제로 레이아웃 갱신
        layoutGuest.invalidate()
        layoutLoggedIn.invalidate()
        btnLogout.invalidate()
    }

    /**
     * 로그아웃 확인 다이얼로그
     */
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("로그아웃")
            .setMessage("로그아웃 하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                performLogout()
            }
            .setNegativeButton("취소", null)
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
                    showToast("로그아웃 되었습니다")
                }.onFailure { error ->
                    Log.e(TAG, "로그아웃 실패: ${error.message}")
                    // 실패해도 로컬 토큰은 이미 삭제됨
                    showToast("로그아웃 되었습니다")
                }

                // UI 업데이트 (로컬 토큰이 삭제되었으므로 비로그인 상태로 표시)
                updateUI()

            } catch (e: Exception) {
                Log.e(TAG, "로그아웃 오류", e)
                showToast("로그아웃 되었습니다")
                updateUI()
            }
        }
    }

    /**
     * 회원탈퇴 확인 다이얼로그
     */
    private fun showWithdrawalDialog() {
        AlertDialog.Builder(this)
            .setTitle("회원탈퇴")
            .setMessage("정말 탈퇴하시겠습니까?\n모든 데이터가 삭제됩니다.")
            .setPositiveButton("탈퇴") { _, _ ->
                performWithdrawal()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    /**
     * 회원탈퇴 실행
     */
    private fun performWithdrawal() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "회원탈퇴 시작")

                val result = authRepository.withdrawal()

                result.onSuccess {
                    Log.d(TAG, "회원탈퇴 성공")
                    showToast("회원탈퇴가 완료되었습니다")
                    // 로그인 화면으로 이동
                    val intent = Intent(this@MyPageActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }.onFailure { error ->
                    Log.e(TAG, "회원탈퇴 실패: ${error.message}")
                    showToast("회원탈퇴 실패: ${error.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "회원탈퇴 중 오류", e)
                showToast("회원탈퇴 중 오류가 발생했습니다")
            }
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}