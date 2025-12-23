package com.project.stampy

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.AuthRepository
import kotlinx.coroutines.launch

class NicknameActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var etNickname: EditText
    private lateinit var btnComplete: MaterialButton

    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository

    companion object {
        private const val TAG = "NicknameActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nickname)

        // TokenManager 및 Repository 초기화
        tokenManager = TokenManager(this)
        RetrofitClient.initialize(tokenManager)
        authRepository = AuthRepository(tokenManager)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        etNickname = findViewById(R.id.et_nickname)
        btnComplete = findViewById(R.id.btn_complete)

        // 버튼 텍스트 설정
        btnComplete.text = "확인"

        // 초기 상태: 버튼 비활성화
        btnComplete.isEnabled = false
    }

    private fun setupListeners() {
        // 뒤로가기
        btnBack.setOnClickListener {
            finish()
        }

        // 닉네임 입력 감지
        etNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 입력이 있으면 버튼 활성화
                btnComplete.isEnabled = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 완료 버튼
        btnComplete.setOnClickListener {
            val nickname = etNickname.text.toString().trim()
            if (nickname.isNotEmpty()) {
                setNickname(nickname)
            } else {
                Toast.makeText(this, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 닉네임 설정 API 호출
     */
    private fun setNickname(nickname: String) {
        lifecycleScope.launch {
            try {
                val result = authRepository.setNickname(nickname)

                result.onSuccess { response ->
                    Log.d(TAG, "닉네임 설정 성공: ${response.nickname}")
                    Toast.makeText(
                        this@NicknameActivity,
                        "닉네임이 설정되었습니다",
                        Toast.LENGTH_SHORT
                    ).show()

                    // 메인 화면으로 이동
                    navigateToMain()
                }.onFailure { error ->
                    Log.e(TAG, "닉네임 설정 실패: ${error.message}")
                    Toast.makeText(
                        this@NicknameActivity,
                        "닉네임 설정 실패: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "닉네임 설정 오류", e)
                Toast.makeText(
                    this@NicknameActivity,
                    "닉네임 설정 중 오류가 발생했습니다",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * 메인 화면으로 이동
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}