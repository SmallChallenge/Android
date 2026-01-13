package com.project.stampy.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.project.stampy.R

/**
 * 커스텀 Toast 확장 함수
 *
 * 기존 Toast.makeText() 대신 Context.showToast()를 사용
 *
 * // 기존 코드 (변경 전):
 * Toast.makeText(this, "메시지", Toast.LENGTH_SHORT).show()
 * Toast.makeText(this, "메시지", Toast.LENGTH_LONG).show()
 *
 * // 새 코드 (변경 후):
 * showToast("메시지")  // LENGTH_SHORT (기본값)
 * showToast("메시지", Toast.LENGTH_LONG)  // LENGTH_LONG
 *
 * // 또는 더 명확하게:
 * showShortToast("메시지")
 * showLongToast("메시지")
 *
 */

/**
 * 커스텀 토스트 메시지 표시
 *
 * @param message 표시할 메시지
 * @param duration 표시 시간 (Toast.LENGTH_SHORT 또는 Toast.LENGTH_LONG)
 */

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    val inflater = LayoutInflater.from(this)
    val layout = inflater.inflate(R.layout.layout_custom_toast, null)

    val textView = layout.findViewById<TextView>(R.id.tv_toast_message)
    textView.text = message

    Toast(this).apply {
        setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
        this.duration = duration
        view = layout
    }.show()
}

/**
 * 커스텀 토스트 메시지 표시 (Fragment용)
 *
 * @param message 표시할 메시지
 * @param duration 표시 시간 (Toast.LENGTH_SHORT 또는 Toast.LENGTH_LONG)
 */
fun Fragment.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    requireContext().showToast(message, duration)
}

/**
 * 짧은 토스트 메시지 (LENGTH_SHORT) - Context용
 */
fun Context.showShortToast(message: String) {
    showToast(message, Toast.LENGTH_SHORT)
}

/**
 * 긴 토스트 메시지 (LENGTH_LONG) - Context용
 */
fun Context.showLongToast(message: String) {
    showToast(message, Toast.LENGTH_LONG)
}

/**
 * 짧은 토스트 메시지 (LENGTH_SHORT) - Fragment용
 */
fun Fragment.showShortToast(message: String) {
    showToast(message, Toast.LENGTH_SHORT)
}

/**
 * 긴 토스트 메시지 (LENGTH_LONG) - Fragment용
 */
fun Fragment.showLongToast(message: String) {
    showToast(message, Toast.LENGTH_LONG)
}