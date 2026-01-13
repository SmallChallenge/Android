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
 * 2버튼 모달 다이얼로그
 *
 * 사용 예시:
 * DoubleButtonDialog(context)
 *     .setTitle("제목")
 *     .setDescription("설명") // 선택사항
 *     .setCancelButtonText("취소") // 선택사항 (기본값: "취소")
 *     .setConfirmButtonText("확인") // 선택사항 (기본값: "확인")
 *     .setOnCancelListener {
 *         // 취소 버튼 클릭 시 동작
 *     }
 *     .setOnConfirmListener {
 *         // 확인 버튼 클릭 시 동작
 *     }
 *     .show()
 */
class DoubleButtonDialog(context: Context) : Dialog(context) {

    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnConfirm: MaterialButton

    private var title: String = ""
    private var description: String? = null
    private var cancelButtonText: String = "취소"
    private var confirmButtonText: String = "확인"
    private var onCancelListener: (() -> Unit)? = null
    private var onConfirmListener: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_double_button)

        // 배경 투명 설정
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 크기 및 중앙 배치
        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels - 60.dpToPx().toInt()

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
        btnCancel = findViewById(R.id.btn_cancel)
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
        btnCancel.text = cancelButtonText
        btnConfirm.text = confirmButtonText
    }

    private fun setupListeners() {
        btnCancel.setOnClickListener {
            onCancelListener?.invoke()
            dismiss()
        }

        btnConfirm.setOnClickListener {
            onConfirmListener?.invoke()
            dismiss()
        }
    }

    /**
     * 제목 설정 (필수)
     */
    fun setTitle(title: String): DoubleButtonDialog {
        this.title = title
        return this
    }

    /**
     * 설명 설정 (선택)
     */
    fun setDescription(description: String?): DoubleButtonDialog {
        this.description = description
        return this
    }

    /**
     * 취소 버튼 텍스트 설정 (선택, 기본값: "취소")
     */
    fun setCancelButtonText(text: String): DoubleButtonDialog {
        this.cancelButtonText = text
        return this
    }

    /**
     * 확인 버튼 텍스트 설정 (선택, 기본값: "확인")
     */
    fun setConfirmButtonText(text: String): DoubleButtonDialog {
        this.confirmButtonText = text
        return this
    }

    /**
     * 취소 버튼 클릭 리스너
     */
    fun setOnCancelListener(listener: () -> Unit): DoubleButtonDialog {
        this.onCancelListener = listener
        return this
    }

    /**
     * 확인 버튼 클릭 리스너
     */
    fun setOnConfirmListener(listener: () -> Unit): DoubleButtonDialog {
        this.onConfirmListener = listener
        return this
    }
}