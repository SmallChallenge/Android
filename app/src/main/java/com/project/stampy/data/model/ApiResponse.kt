package com.project.stampy.data.model

import com.google.gson.annotations.SerializedName

/**
 * 공통 API 응답 래퍼
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean = false,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("timestamp")
    val timestamp: String = "",

    // 에러 응답용 필드
    @SerializedName("code")
    val code: String? = null,

    @SerializedName("message")
    val message: String? = null
) {
    fun toResult(): Result<T> {
        return if (success && data != null) {
            Result.success(data)
        } else {
            Result.failure(Exception(message ?: "알 수 없는 오류"))
        }
    }
}