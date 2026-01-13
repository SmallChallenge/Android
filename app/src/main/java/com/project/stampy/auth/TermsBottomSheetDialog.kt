package com.project.stampy.auth

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.project.stampy.R
import com.project.stampy.etc.WebViewActivity
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.network.RetrofitClient
import com.project.stampy.data.repository.AuthRepository
import com.project.stampy.etc.SingleButtonDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TermsBottomSheetDialog : BottomSheetDialogFragment() {

    // 레이아웃
    private lateinit var layoutAllAgree: LinearLayout

    // 체크박스
    private lateinit var ivCheckAll: ImageView
    private lateinit var ivCheckTerms: ImageView
    private lateinit var ivCheckPrivacy: ImageView

    // 클릭 영역
    private lateinit var btnCheckTerms: LinearLayout
    private lateinit var btnViewTerms: FrameLayout
    private lateinit var btnCheckPrivacy: LinearLayout
    private lateinit var btnViewPrivacy: FrameLayout

    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository

    private var isTermsChecked = false
    private var isPrivacyChecked = false

    companion object {
        private const val TAG = "TermsBottomSheet"
        private const val TERMS_URL = "https://sage-hare-ff7.notion.site/2d5f2907580d80df9a21f95acd343d3f?source=copy_link"
        private const val PRIVACY_URL = "https://sage-hare-ff7.notion.site/2d5f2907580d80eda745ccfbda543bc5?source=copy_link"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_terms, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 투명 배경 테마 적용
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)

        // 뒤로가기 버튼 비활성화 (onCancel로 처리)
        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as BottomSheetDialog)
                .findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)

            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false

                // 배경 투명 설정
                it.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        }

        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TokenManager 및 Repository 초기화
        tokenManager = TokenManager(requireContext())
        RetrofitClient.initialize(tokenManager)
        authRepository = AuthRepository(tokenManager)

        initViews(view)
        setupListeners()
    }

    private fun initViews(view: View) {
        // 레이아웃
        layoutAllAgree = view.findViewById(R.id.layout_all_agree)

        // 체크박스
        ivCheckAll = view.findViewById(R.id.iv_check_all)
        ivCheckTerms = view.findViewById(R.id.iv_check_terms)
        ivCheckPrivacy = view.findViewById(R.id.iv_check_privacy)

        // 클릭 영역
        btnCheckTerms = view.findViewById(R.id.btn_check_terms)
        btnViewTerms = view.findViewById(R.id.btn_view_terms)
        btnCheckPrivacy = view.findViewById(R.id.btn_check_privacy)
        btnViewPrivacy = view.findViewById(R.id.btn_view_privacy)
    }

    private fun setupListeners() {
        // 전체 동의 레이아웃 전체 클릭
        layoutAllAgree.setOnClickListener {
            toggleAllAgree()
        }

        // 이용약관 - 체크박스 영역만 클릭
        btnCheckTerms.setOnClickListener {
            toggleTermsCheck()
        }

        // 이용약관 보기 (화살표)
        btnViewTerms.setOnClickListener {
            openWebView(TERMS_URL)
        }

        // 개인정보 - 체크박스 영역만 클릭
        btnCheckPrivacy.setOnClickListener {
            togglePrivacyCheck()
        }

        // 개인정보 보기 (화살표)
        btnViewPrivacy.setOnClickListener {
            openWebView(PRIVACY_URL)
        }
    }

    /**
     * 뒤로가기 버튼 처리 - 가입 취소
     */
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Log.d(TAG, "뒤로가기 버튼 - 가입 취소 처리")
        performCancelRegistration()
    }

    /**
     * 가입 취소 API 호출 (PENDING 상태에서 나가기)
     */
    private fun performCancelRegistration() {
        GlobalScope.launch(Dispatchers.Main) {
            Log.d(TAG, "가입 취소 시도")

            val result = authRepository.cancelRegistration()

            result.onSuccess {
                Log.d(TAG, "가입 취소 성공")
            }.onFailure { error ->
                Log.e(TAG, "가입 취소 실패: ${error.message}")
            }

            // 성공/실패 관계없이 LoginActivity 종료
            withContext(Dispatchers.Main) {
                activity?.finish()
            }
        }
    }

    /**
     * 전체 동의 토글
     */
    private fun toggleAllAgree() {
        val newState = !(isTermsChecked && isPrivacyChecked)
        isTermsChecked = newState
        isPrivacyChecked = newState
        updateAllCheckboxes()

        // 전체 동의 시 자동으로 약관 동의 API 호출
        if (isTermsChecked && isPrivacyChecked) {
            agreeTermsAndStart()
        }
    }

    /**
     * 이용약관 토글
     */
    private fun toggleTermsCheck() {
        isTermsChecked = !isTermsChecked
        updateAllCheckboxes()

        // 둘 다 체크되면 자동으로 약관 동의 API 호출
        if (isTermsChecked && isPrivacyChecked) {
            agreeTermsAndStart()
        }
    }

    /**
     * 개인정보 토글
     */
    private fun togglePrivacyCheck() {
        isPrivacyChecked = !isPrivacyChecked
        updateAllCheckboxes()

        // 둘 다 체크되면 자동으로 약관 동의 API 호출
        if (isTermsChecked && isPrivacyChecked) {
            agreeTermsAndStart()
        }
    }

    /**
     * 모든 체크박스 업데이트
     */
    private fun updateAllCheckboxes() {
        // 전체 동의 체크박스
        if (isTermsChecked && isPrivacyChecked) {
            ivCheckAll.setImageResource(R.drawable.ic_checkbox_checked)
        } else {
            ivCheckAll.setImageResource(R.drawable.ic_checkbox_unchecked)
        }

        // 이용약관 체크박스
        if (isTermsChecked) {
            ivCheckTerms.setImageResource(R.drawable.ic_checkbox_checked)
        } else {
            ivCheckTerms.setImageResource(R.drawable.ic_checkbox_unchecked)
        }

        // 개인정보 체크박스
        if (isPrivacyChecked) {
            ivCheckPrivacy.setImageResource(R.drawable.ic_checkbox_checked)
        } else {
            ivCheckPrivacy.setImageResource(R.drawable.ic_checkbox_unchecked)
        }
    }

    /**
     * 웹뷰로 약관 보기
     */
    private fun openWebView(url: String) {
        val intent = Intent(requireContext(), WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.EXTRA_URL, url)
        startActivity(intent)
    }

    /**
     * 약관 동의 API 호출 후 닉네임 설정으로 이동
     */
    private fun agreeTermsAndStart() {
        Log.d(TAG, "약관 동의 API 호출")

        lifecycleScope.launch {
            try {
                val result = authRepository.agreeTerms(
                    agreedToPrivacyPolicy = isPrivacyChecked,
                    agreedToTermsOfService = isTermsChecked,
                    agreedToMarketing = false
                )

                result.onSuccess { response ->
                    Log.d(TAG, "약관 동의 성공: userStatus=${response.userStatus}")

                    // 닉네임 설정으로 이동
                    dismiss()
                    navigateToNickname()

                }.onFailure { error ->
                    Log.e(TAG, "약관 동의 실패: ${error.message}")
                    showErrorDialog()
                }
            } catch (e: Exception) {
                Log.e(TAG, "약관 동의 오류", e)
                showErrorDialog()
            }
        }
    }

    /**
     * 에러 다이얼로그 표시
     */
    private fun showErrorDialog() {
        SingleButtonDialog(requireContext())
            .setTitle("약관 동의에 실패했어요.\n다시 시도해주세요.")
            .show()
    }

    /**
     * 닉네임 설정으로 이동
     */
    private fun navigateToNickname() {
        val intent = Intent(requireContext(), NicknameActivity::class.java)
        startActivity(intent)
        requireActivity().finish()  // LoginActivity 종료
    }
}