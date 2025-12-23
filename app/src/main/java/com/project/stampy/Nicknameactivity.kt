package com.project.stampy

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.AuthRepository
import kotlinx.coroutines.launch

class NicknameActivity : AppCompatActivity() {

    private lateinit var layoutContent: ConstraintLayout
    private lateinit var layoutInput: FrameLayout
    private lateinit var etNickname: EditText
    private lateinit var viewUnderline: View
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
        setupKeyboardListener()
    }

    private fun initViews() {
        layoutContent = findViewById(R.id.layout_content)
        layoutInput = findViewById(R.id.layout_input)
        etNickname = findViewById(R.id.et_nickname)
        viewUnderline = findViewById(R.id.view_underline)
        btnComplete = findViewById(R.id.btn_complete)

        // 버튼 텍스트 설정
        btnComplete.text = "확인"

        // 초기 상태: 버튼 비활성화
        btnComplete.isEnabled = false
    }

    private fun setupListeners() {
        // 입력 영역 전체 클릭 시 EditText 포커스
        layoutInput.setOnClickListener {
            etNickname.requestFocus()
        }

        // 닉네임 입력 감지
        etNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s?.toString() ?: ""
                btnComplete.isEnabled = input.isNotEmpty() && isValidNickname(input)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 포커스 변경 감지 (밑줄 색상 변경)
        etNickname.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                viewUnderline.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.neon_primary)
                )
            } else {
                viewUnderline.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.gray_700)
                )
            }
        }

        // 완료 버튼
        btnComplete.setOnClickListener {
            val nickname = etNickname.text.toString().trim()
            if (nickname.isNotEmpty()) {
                setNickname(nickname)
            } else {
                Toast.makeText(this, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        // 빈 영역 클릭 시 포커스 해제
        findViewById<View>(android.R.id.content).setOnClickListener {
            etNickname.clearFocus()
        }
    }

    private fun setupKeyboardListener() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

            if (imeVisible && imeHeight > 0) {
                // 키보드 올라옴 - 버튼을 키보드 바로 위로
                val params = btnComplete.layoutParams as ConstraintLayout.LayoutParams
                params.bottomMargin = imeHeight + 40
                btnComplete.layoutParams = params
            } else {
                // 키보드 내려감 - 원래 위치로
                val params = btnComplete.layoutParams as ConstraintLayout.LayoutParams
                params.bottomMargin = 40
                btnComplete.layoutParams = params
            }

            insets
        }
    }

    /**
     * 닉네임 설정 API 호출
     */
    private fun isValidNickname(nickname: String): Boolean {
        val regex = Regex("^[가-힣a-zA-Z0-9]{1,10}$")
        return regex.matches(nickname)
    }

    private fun setNickname(nickname: String) {
        if (!isValidNickname(nickname)) {
            Toast.makeText(
                this,
                "닉네임은 한글, 영문, 숫자만 1-10자로 입력해주세요",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

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

    override fun onBackPressed() {
        navigateToMyPage()
    }

    private fun navigateToMyPage() {
        val intent = Intent(this, MyPageActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}