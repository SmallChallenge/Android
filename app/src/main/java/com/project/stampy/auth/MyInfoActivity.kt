package com.project.stampy.auth

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.project.stampy.R
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.AuthRepository
import com.project.stampy.etc.DoubleButtonDialog
import com.project.stampy.etc.SingleButtonDialog
import com.project.stampy.utils.showToast
import kotlinx.coroutines.launch

class MyInfoActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvNicknameValue: TextView
    private lateinit var btnWithdraw: LinearLayout
    private lateinit var layoutNickname: LinearLayout

    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository

    // 닉네임 변경 결과를 받기 위한 launcher
    private val nicknameEditLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // 닉네임이 변경되었으면 업데이트
            val updatedNickname = tokenManager.getNickname()
            tvNicknameValue.text = updatedNickname ?: "사용자"

            showToast("저장이 완료되었습니다.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_info)

        tokenManager = TokenManager(this)
        RetrofitClient.initialize(tokenManager)
        authRepository = AuthRepository(tokenManager)

        initViews()
        setupListeners()

        // 데이터 설정
        tvNicknameValue.text = tokenManager.getNickname() ?: "사용자"
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        tvNicknameValue = findViewById(R.id.tv_nickname_value)
        btnWithdraw = findViewById(R.id.btn_withdraw)
        layoutNickname = findViewById(R.id.layout_nickname)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        // 닉네임 레이아웃 클릭 시 닉네임 수정 페이지로 이동
        layoutNickname.setOnClickListener {
            val intent = Intent(this, NicknameActivity::class.java)
            intent.putExtra("MODE", "EDIT") // 수정 모드임을 전달
            intent.putExtra("CURRENT_NICKNAME", tokenManager.getNickname())
            nicknameEditLauncher.launch(intent)
        }

        // 회원탈퇴 클릭 시 모달 노출
        btnWithdraw.setOnClickListener {
            showWithdrawDialog()
        }
    }

    private fun showWithdrawDialog() {
        DoubleButtonDialog(this)
            .setTitle("탈퇴하시겠습니까?")
            .setDescription("탈퇴 후 7일 이내 재로그인 시 데이터가 복구됩니다.\n7일 이후에는 모든 정보가 영구 삭제됩니다.")
            .setCancelButtonText("취소")
            .setConfirmButtonText("탈퇴")
            .setOnConfirmListener {
                performWithdraw()
            }
            .show()
    }

    private fun performWithdraw() {
        lifecycleScope.launch {
            val result = authRepository.withdrawal()  // ACTIVE 상태에서 회원탈퇴

            result.onSuccess {
                // 성공 시 로그인 화면으로
                val intent = Intent(this@MyInfoActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }.onFailure {
                showWithdrawErrorDialog()
            }
        }
    }

    private fun showWithdrawErrorDialog() {
        SingleButtonDialog(this)
            .setTitle("탈퇴에 실패했어요.\n다시 시도해주세요.")
            .show()
    }
}