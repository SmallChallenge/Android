package com.project.stampy.auth

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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.project.stampy.MainActivity
import com.project.stampy.R
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.AuthRepository
import com.project.stampy.etc.SingleButtonDialog
import com.project.stampy.utils.showToast
import kotlinx.coroutines.launch

class NicknameActivity : AppCompatActivity() {

    private lateinit var layoutContent: ConstraintLayout
    private lateinit var layoutInput: FrameLayout
    private lateinit var etNickname: EditText
    private lateinit var viewUnderline: View
    private lateinit var tvError: TextView
    private lateinit var btnComplete: MaterialButton
    private lateinit var btnBackTouchArea: FrameLayout
    private lateinit var btnCloseTouchArea: FrameLayout
    private lateinit var tvTitle: TextView

    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository

    private var isEditMode = false
    private var currentNickname: String? = null

    companion object {
        private const val TAG = "NicknameActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nickname)

        // 모드 확인 (신규 가입 or 수정)
        isEditMode = intent.getStringExtra("MODE") == "EDIT"
        currentNickname = intent.getStringExtra("CURRENT_NICKNAME")

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

        // 수정 모드일 경우 현재 닉네임 표시
        if (isEditMode && currentNickname != null) {
            etNickname.setText(currentNickname)
            etNickname.setSelection(currentNickname!!.length) // 커서를 끝으로
            validateNickname(currentNickname!!)
        }

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
        tvError = findViewById(R.id.tv_error)
        btnComplete = findViewById(R.id.btn_complete)
        btnBackTouchArea = findViewById(R.id.btn_back_touch_area)
        btnCloseTouchArea = findViewById(R.id.btn_close_touch_area)
        tvTitle = findViewById(R.id.tv_title)

        if (isEditMode) {
            // 수정 모드일 경우 닫기 버튼 숨김
            btnCloseTouchArea.visibility = View.GONE
        }

        // 버튼 텍스트 설정
        btnComplete.text = "확인"

        // 초기 상태: 버튼 비활성화
        btnComplete.isEnabled = false
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isEditMode) {
                    // 수정 모드: 그냥 종료
                    finish()
                } else {
                    // 신규 가입 모드: 토큰 삭제하고 LoginActivity로 돌아가기
                    tokenManager.clearTokens()
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
        })
    }

    private fun setupListeners() {
        // 뒤로가기 버튼
        btnBackTouchArea.setOnClickListener {
            if (isEditMode) {
                finish()
            } else {
                tokenManager.clearTokens()
                setResult(RESULT_CANCELED)
                finish()
            }
        }

        // 닫기 버튼 (신규 가입 모드에만 표시)
        btnCloseTouchArea.setOnClickListener {
            cancelLoginFlow()
        }

        layoutInput.setOnClickListener {
            etNickname.requestFocus()
        }

        // 닉네임 입력 감지 - 실시간 유효성 검사
        etNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s?.toString() ?: ""
                validateNickname(input)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 포커스 변경 감지
        etNickname.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // 포커스 얻었을 때: 즉시 Active 색상으로 변경
                updateColorsForFocus(true)
            } else {
                // 포커스 잃었을 때: 입력값 여부에 따라 색상 결정
                val hasInput = etNickname.text.toString().isNotEmpty()
                updateColors(hasInput)
            }
        }

        btnComplete.setOnClickListener {
            val nickname = etNickname.text.toString().trim()
            if (nickname.isNotEmpty()) {
                setNickname(nickname)
            } else {
                showToast("닉네임을 입력해주세요")
            }
        }

        // 빈 영역 클릭 시 포커스 해제
        findViewById<View>(android.R.id.content).setOnClickListener {
            etNickname.clearFocus()
        }
    }

    /**
     * 닉네임 실시간 유효성 검사
     */
    private fun validateNickname(nickname: String) {
        // 에러 상태 초기화
        val wasError = tvError.visibility == View.VISIBLE
        tvError.visibility = View.GONE

        when {
            // 빈 값
            nickname.isEmpty() -> {
                btnComplete.isEnabled = false
                // 포커스가 있으면 Active 색상 유지
                if (etNickname.hasFocus()) {
                    updateColorsForFocus(true)
                } else {
                    updateColors(false)
                }
            }
            // 현재 닉네임과 동일 (수정 모드인 경우)
            isEditMode && nickname == currentNickname -> {
                btnComplete.isEnabled = false
                if (etNickname.hasFocus()) {
                    updateColorsForFocus(true)
                } else {
                    updateColors(false)
                }
            }
            // 2자 미만, 10자 초과
            nickname.length < 2 || nickname.length > 10 -> {
                btnComplete.isEnabled = false
                showError("닉네임은 2~10자로 입력해주세요.")
            }
            // 특수문자 또는 공백 포함 (한글, 영문, 숫자만 허용)
            !nickname.matches(Regex("^[가-힣a-zA-Z0-9]+$")) -> {
                btnComplete.isEnabled = false
                showError("닉네임은 공백 없이 한글, 영문, 숫자만 가능해요.")
            }
            // 유효한 닉네임
            else -> {
                btnComplete.isEnabled = true
                updateColors(true)
            }
        }

        // 에러가 사라진 경우 색상 복구
        if (wasError && tvError.visibility == View.GONE && etNickname.hasFocus()) {
            updateColorsForFocus(true)
        }
    }

    /**
     * 에러 메시지 표시
     */
    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE

        // 에러 상태
        viewUnderline.setBackgroundColor(
            ContextCompat.getColor(this, R.color.error_red)
        )

        // 버튼은 비활성화 상태 유지
        btnComplete.isEnabled = false
        btnComplete.backgroundTintList = ContextCompat.getColorStateList(this, R.color.button_inactive)
        btnComplete.setTextColor(ContextCompat.getColor(this, R.color.button_text_inactive))
    }

    /**
     * 포커스 상태에 따른 색상 업데이트 (에러 없을 때)
     */
    private fun updateColorsForFocus(hasFocus: Boolean) {
        // 에러 메시지가 있으면 색상 변경 안 함
        if (tvError.visibility == View.VISIBLE) {
            return
        }

        if (hasFocus) {
            // 포커스 있음: Active 색상 (네온)
            viewUnderline.setBackgroundColor(
                ContextCompat.getColor(this, R.color.neon_primary)
            )

            // 버튼도 Active 색상 (입력값과 무관)
            btnComplete.backgroundTintList = ContextCompat.getColorStateList(this, R.color.neon_primary)
            btnComplete.setTextColor(ContextCompat.getColor(this, R.color.button_text_active))
        } else {
            // 포커스 없음: 회색
            viewUnderline.setBackgroundColor(
                ContextCompat.getColor(this, R.color.gray_700)
            )
            btnComplete.backgroundTintList = ContextCompat.getColorStateList(this, R.color.button_inactive)
            btnComplete.setTextColor(ContextCompat.getColor(this, R.color.button_text_inactive))
        }
    }

    /**
     * 밑줄과 버튼 색상 업데이트 (입력값 기준)
     */
    private fun updateColors(hasInput: Boolean) {
        // 에러 메시지가 있으면 색상 변경 안 함
        if (tvError.visibility == View.VISIBLE) {
            return
        }

        if (hasInput) {
            // 입력값 있으면 네온
            viewUnderline.setBackgroundColor(
                ContextCompat.getColor(this, R.color.neon_primary)
            )
            btnComplete.backgroundTintList = ContextCompat.getColorStateList(this, R.color.neon_primary)
            btnComplete.setTextColor(ContextCompat.getColor(this, R.color.button_text_active))
        } else {
            // 입력값 없으면 회색
            viewUnderline.setBackgroundColor(
                ContextCompat.getColor(this, R.color.gray_700)
            )
            btnComplete.backgroundTintList = ContextCompat.getColorStateList(this, R.color.button_inactive)
            btnComplete.setTextColor(ContextCompat.getColor(this, R.color.button_text_inactive))
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
    }

    private fun onKeyboardHidden() {
        Log.d(TAG, "Keyboard HIDDEN")

        // 컨텐츠 원래 위치로
        val contentParams = layoutContent.layoutParams as ConstraintLayout.LayoutParams
        contentParams.topMargin = 0
        layoutContent.layoutParams = contentParams
    }

    /**
     * 닉네임 설정 API 호출
     */
    private fun isValidNickname(nickname: String): Boolean {
        val regex = Regex("^[가-힣a-zA-Z0-9]{2,10}$")
        return regex.matches(nickname)
    }

    private fun setNickname(nickname: String) {
        if (!isValidNickname(nickname)) {
            showToast("닉네임은 한글, 영문, 숫자만 2-10자로 입력해주세요")
            return
        }

        lifecycleScope.launch {
            try {
                val result = authRepository.setNickname(nickname)

                result.onSuccess { response ->
                    Log.d(TAG, "닉네임 설정 성공: ${response.nickname}")

                    if (isEditMode) {
                        // 수정 화면: 환영 메시지 표시 후 이전 화면으로 돌아가기
                        showToast("반가워요, ${response.nickname ?: nickname}님! 이제 기록을 시작해볼까요?", Toast.LENGTH_LONG)
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        // 신규 가입 모드: 메인 화면으로 이동하면서 환영 메시지 전달
                        navigateToMain(response.nickname ?: nickname)
                    }
                }.onFailure { error ->
                    Log.e(TAG, "닉네임 설정 실패: ${error.message}")

                    // 409 에러 코드 또는 중복 관련 메시지 확인
                    val errorMessage = error.message ?: ""
                    if (errorMessage.contains("409") ||
                        errorMessage.contains("중복") ||
                        errorMessage.contains("이미") ||
                        errorMessage.contains("duplicate") ||
                        errorMessage.contains("already") ||
                        errorMessage.contains("exist")) {
                        // 중복 닉네임
                        showError("이미 누군가 사용하고 있어요.")
                        btnComplete.isEnabled = false
                        updateColors(false)
                    } else {
                        // 기타 에러
                        if (isEditMode) {
                            showSaveFailedDialog()
                        } else {
                            showSignUpFailedDialog()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "닉네임 설정 오류", e)
                if (isEditMode) {
                    showSaveFailedDialog()
                } else {
                    showSignUpFailedDialog()
                }
            }
        }
    }

    /**
     * 저장 실패 모달 표시 (수정 모드)
     */
    private fun showSaveFailedDialog() {
        SingleButtonDialog(this)
            .setTitle("저장에 실패했어요.\n다시 시도해주세요.")
            .show()
    }

    /**
     * 회원가입 실패 모달 표시 (신규 가입 모드)
     */
    private fun showSignUpFailedDialog() {
        SingleButtonDialog(this)
            .setTitle("회원가입에 실패했어요.\n다시 시도해주세요.")
            .show()
    }

    /**
     * 메인 화면으로 이동
     */
    private fun navigateToMain(nickname: String) {
        // 토스트 메시지 표시
        showToast("반가워요, ${nickname}님! 이제 기록을 시작해볼까요?", Toast.LENGTH_LONG)

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * 로그인 플로우 전체 취소 (닫기 버튼)
     */
    private fun cancelLoginFlow() {
        tokenManager.clearTokens()
        val intent = Intent()
        intent.putExtra("FROM_CLOSE", true) // 닫기 버튼임을 표시
        setResult(RESULT_CANCELED, intent)
        finish()
    }
}