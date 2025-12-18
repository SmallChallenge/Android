package com.project.stampy

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var btnKakaoLogin: FrameLayout
    private lateinit var btnGoogleLogin: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupListeners()
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
            // TODO: 카카오 로그인 구현
            Toast.makeText(this, "카카오 로그인 준비중", Toast.LENGTH_SHORT).show()
        }

        // 구글 로그인
        btnGoogleLogin.setOnClickListener {
            // TODO: 구글 로그인 구현
            Toast.makeText(this, "구글 로그인 준비중", Toast.LENGTH_SHORT).show()
        }
    }
}