package com.project.stampy

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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
    private lateinit var btnBackTouchArea: FrameLayout
    private lateinit var btnCloseTouchArea: FrameLayout

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
        setupBackPressHandler()

        // 아이콘 색상 변경 (흰색)
        setIconColors()

        // 초기 포커스 (커서 깜빡임)
        etNickname.post {
            etNickname.requestFocus()
        }
    }

    /**
     * 뒤로가기 아이콘 색상을 흰색으로 변경
     */
    private fun setIconColors() {
        val ivBack = findViewById<ImageView>(R.id.iv_back)
        val ivClose = findViewById<ImageView>(R.id.iv_close)

        ivBack.setColorFilter(
            ContextCompat.getColor(this, android.R.color.white),
            android.graphics.PorterDuff.Mode.SRC_IN
        )

        ivClose.setColorFilter(
            ContextCompat.getColor(this, android.R.color.white),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }

    private fun initViews() {
        layoutContent = findViewById(R.id.layout_content)
        layoutInput = findViewById(R.id.layout_input)
        etNickname = findViewById(R.id.et_nickname)
        viewUnderline = findViewById(R.id.view_underline)
        btnComplete = findViewById(R.id.btn_complete)
        btnBackTouchArea = findViewById(R.id.btn_back_touch_area)
        btnCloseTouchArea = findViewById(R.id.btn_close_touch_area)

        // 버튼 텍스트 설정
        btnComplete.text = "확인"

        // 초기 상태: 버튼 비활성화
        btnComplete.isEnabled = false
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToMyPage()
            }
        })
    }

    private fun setupListeners() {
        // 뒤로가기 버튼 - 로그인 페이지로
        btnBackTouchArea.setOnClickListener {
            navigateToLogin()
        }

        // 닫기 버튼 - 그냥 finish() (LoginActivity와 함께 스택에서 제거됨)
        btnCloseTouchArea.setOnClickListener {
            // LoginActivity의 결과로 취소 전달
            setResult(RESULT_CANCELED)
            finish()
        }

        layoutInput.setOnClickListener {
            etNickname.requestFocus()
        }

        // 닉네임 입력 감지
        etNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s?.toString() ?: ""
                val isValid = input.isNotEmpty() && isValidNickname(input)

                // 버튼 활성화 상태
                btnComplete.isEnabled = isValid

                // 입력값 있으면 네온, 없으면 회색
                updateColors(input.isNotEmpty())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 포커스 변경 감지
        etNickname.setOnFocusChangeListener { _, hasFocus ->
            val hasInput = etNickname.text.toString().isNotEmpty()
            updateColors(hasInput)
        }

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

    /**
     * 밑줄과 버튼 색상 업데이트
     */
    private fun updateColors(hasInput: Boolean) {
        if (hasInput) {
            // 입력값 있으면 네온
            viewUnderline.setBackgroundColor(
                ContextCompat.getColor(this, R.color.neon_primary)
            )
            btnComplete.backgroundTintList = ContextCompat.getColorStateList(this, R.color.neon_primary)
            btnComplete.setTextColor(
                ContextCompat.getColor(this, R.color.button_text_active)
            )
        } else {
            // 입력값 없으면 회색
            viewUnderline.setBackgroundColor(
                ContextCompat.getColor(this, R.color.gray_700)
            )
            btnComplete.backgroundTintList = ContextCompat.getColorStateList(this, R.color.button_inactive)
            btnComplete.setTextColor(
                ContextCompat.getColor(this, R.color.button_text_inactive)
            )
        }
    }

    private fun setupKeyboardListener() {
        val rootView = window.decorView.findViewById<View>(android.R.id.content)

        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            private var wasKeyboardVisible = false

            override fun onGlobalLayout() {
                val rect = android.graphics.Rect()
                rootView.getWindowVisibleDisplayFrame(rect)

                val screenHeight = rootView.rootView.height
                val keypadHeight = screenHeight - rect.bottom

                val isKeyboardVisible = keypadHeight > screenHeight * 0.15

                Log.d(TAG, "Screen: $screenHeight, Keypad: $keypadHeight, Visible: $isKeyboardVisible")

                if (isKeyboardVisible && !wasKeyboardVisible) {
                    onKeyboardShown(keypadHeight)
                } else if (!isKeyboardVisible && wasKeyboardVisible) {
                    onKeyboardHidden()
                }

                wasKeyboardVisible = isKeyboardVisible
            }
        })
    }

    private fun onKeyboardShown(keyboardHeight: Int) {
        Log.d(TAG, "Keyboard SHOWN: $keyboardHeight")

        // 컨텐츠만 살짝 위로 이동 (키보드 높이의 1/4만큼)
        val contentParams = layoutContent.layoutParams as ConstraintLayout.LayoutParams
        contentParams.topMargin = -(keyboardHeight / 4)
        layoutContent.layoutParams = contentParams

        // 확인 버튼은 원래대로 (하단 40dp 고정 - 안 움직임)
    }

    private fun onKeyboardHidden() {
        Log.d(TAG, "Keyboard HIDDEN")

        // 컨텐츠 원래 위치로
        val contentParams = layoutContent.layoutParams as ConstraintLayout.LayoutParams
        contentParams.topMargin = 0
        layoutContent.layoutParams = contentParams

        // 버튼도 원래 위치로
        // 아무것도 안 함
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

    /**
     * 로그인 페이지로 이동 (뒤로가기)
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun navigateToMyPage() {
        val intent = Intent(this, MyPageActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}