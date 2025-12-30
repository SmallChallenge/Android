package com.project.stampy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.AuthRepository
import com.project.stampy.ui.dialog.DoubleButtonDialog
import com.project.stampy.ui.dialog.SingleButtonDialog
import kotlinx.coroutines.launch

class MyInfoActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var tvNicknameValue: TextView
    private lateinit var btnWithdraw: LinearLayout

    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository

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
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

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
            val result = authRepository.withdraw()

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