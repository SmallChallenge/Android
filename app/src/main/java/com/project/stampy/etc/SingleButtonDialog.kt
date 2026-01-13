package com.project.stampy.etc

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.project.stampy.R

/**
 * 1버튼 모달 다이얼로그
 *
 * 사용 예시:
 * SingleButtonDialog(context)
 *     .setTitle("제목")
 *     .setDescription("설명") // 선택사항
 *     .setButtonText("확인") // 선택사항 (기본값: "확인")
 *     .setOnConfirmListener {
 *         // 버튼 클릭 시 동작
 *     }
 *     .show()
 */
class SingleButtonDialog(context: Context) : Dialog(context) {

    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnConfirm: MaterialButton

    private var title: String = ""
    private var description: String? = null
    private var buttonText: String = "확인"
    private var onConfirmListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_single_button)

        // 배경 투명 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 크기 및 중앙 배치
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels - 60.dpToPx().toInt()    //모달 너비: 화면 너비 - 60

        window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(android.view.Gravity.CENTER)

        initViews()
        setupContent()
        setupListeners()
    }

    private fun Int.dpToPx(): Float {
        return this * context.resources.displayMetrics.density
    }

    private fun initViews() {
        tvTitle = findViewById(R.id.tv_title)
        tvDescription = findViewById(R.id.tv_description)
        btnConfirm = findViewById(R.id.btn_confirm)
    }

    private fun setupContent() {
        // 제목 설정
        tvTitle.text = title

        // 설명 설정 (있는 경우만)
        if (description != null) {
            tvDescription.text = description
            tvDescription.visibility = View.VISIBLE
        } else {
            tvDescription.visibility = View.GONE
        }

        // 버튼 텍스트 설정
        btnConfirm.text = buttonText
    }

    private fun setupListeners() {
        btnConfirm.setOnClickListener {
            onConfirmListener?.invoke()
            dismiss()
        }
    }

    /**
     * 제목 설정 (필수)
     */
    fun setTitle(title: String): SingleButtonDialog {
        this.title = title
        return this
    }

    /**
     * 설명 설정 (선택)
     */
    fun setDescription(description: String?): SingleButtonDialog {
        this.description = description
        return this
    }

    /**
     * 버튼 텍스트 설정 (선택, 기본값: "확인")
     */
    fun setButtonText(text: String): SingleButtonDialog {
        this.buttonText = text
        return this
    }

    /**
     * 확인 버튼 클릭 리스너
     */
    fun setOnConfirmListener(listener: () -> Unit): SingleButtonDialog {
        this.onConfirmListener = listener
        return this
    }
}