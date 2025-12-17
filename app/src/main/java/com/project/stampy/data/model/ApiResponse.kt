package com.project.stampy.data.model

import com.google.gson.annotations.SerializedName

/**
 * API 공통 응답 모델
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: T?,

    @SerializedName("timestamp")
    val timestamp: String
)

/**
 * 에러 응답 모델
 */
data class ErrorResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val timestamp: String
)

/**
 * ApiResponse를 Result로 변환하는 확장 함수
 */
fun <T> ApiResponse<T>.toResult(): Result<T> {
    return if (success && data != null) {
        Result.success(data)
    } else {
        Result.failure(Exception(message))
    }
}