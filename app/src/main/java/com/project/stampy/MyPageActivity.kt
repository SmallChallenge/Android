package com.project.stampy

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.project.stampy.util.UserPreferences

class MyPageActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var userPreferences: UserPreferences

    // 로그인 상태 뷰
    private lateinit var layoutLoggedIn: LinearLayout
    private lateinit var ivProfile: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var btnGoToNext: ImageView

    // 비로그인 상태 뷰
    private lateinit var layoutLoggedOut: LinearLayout
    private lateinit var btnLoginSignup: Button

    // 공통 메뉴
    private lateinit var tvAppVersion: TextView
    private lateinit var btnLogout: TextView
    private lateinit var btnDeleteAccount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        userPreferences = UserPreferences.getInstance(this)

        initViews()
        setupListeners()
        updateUI()
    }

    private fun initViews() {
        // 상단 바
        btnBack = findViewById(R.id.btn_back)

        // 로그인 상태 뷰
        layoutLoggedIn = findViewById(R.id.layout_logged_in)
        ivProfile = findViewById(R.id.iv_profile)
        tvUserName = findViewById(R.id.tv_user_name)
        btnGoToNext = findViewById(R.id.btn_go_to_next)

        // 비로그인 상태 뷰
        layoutLoggedOut = findViewById(R.id.layout_logged_out)
        btnLoginSignup = findViewById(R.id.btn_login_signup)

        // 공통 메뉴
        tvAppVersion = findViewById(R.id.tv_app_version)
        btnLogout = findViewById(R.id.btn_logout)
        btnDeleteAccount = findViewById(R.id.btn_delete_account)
    }

    private fun setupListeners() {
        // 뒤로가기
        btnBack.setOnClickListener {
            finish()
        }

        // 로그인/회원가입 버튼
        btnLoginSignup.setOnClickListener {
            // TODO: 로그인 화면으로 이동
            Toast.makeText(this, "로그인 화면으로 이동", Toast.LENGTH_SHORT).show()

            // 테스트용: 로그인 처리
            userPreferences.saveLoginInfo("000", "test@example.com")
            updateUI()
        }

        // 사용자 정보 클릭 (로그인 상태)
        layoutLoggedIn.setOnClickListener {
            // TODO: 프로필 상세 화면으로 이동
            Toast.makeText(this, "프로필 상세 화면으로 이동", Toast.LENGTH_SHORT).show()
        }

        // 로그아웃
        btnLogout.setOnClickListener {
            userPreferences.logout()
            Toast.makeText(this, "로그아웃되었습니다", Toast.LENGTH_SHORT).show()
            updateUI()
        }

        // 탈퇴하기
        btnDeleteAccount.setOnClickListener {
            // TODO: 탈퇴 확인 다이얼로그 표시
            Toast.makeText(this, "탈퇴 기능 준비중", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 로그인 상태에 따라 UI 업데이트
     */
    private fun updateUI() {
        if (userPreferences.isLoggedIn()) {
            // 로그인 상태
            layoutLoggedIn.visibility = View.VISIBLE
            layoutLoggedOut.visibility = View.GONE
            btnLogout.visibility = View.VISIBLE

            // 사용자 정보 표시
            val userName = userPreferences.getUserName() ?: "사용자"
            tvUserName.text = userName

            // 프로필 이미지는 나중에 Glide 등으로 로드
            // TODO: Load profile image with Glide
            // val profileImageUrl = userPreferences.getProfileImageUrl()
            // if (!profileImageUrl.isNullOrEmpty()) {
            //     Glide.with(this).load(profileImageUrl).into(ivProfile)
            // }

        } else {
            // 비로그인 상태
            layoutLoggedIn.visibility = View.GONE
            layoutLoggedOut.visibility = View.VISIBLE
            btnLogout.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }
}