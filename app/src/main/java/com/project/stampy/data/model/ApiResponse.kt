package com.project.stampy.data.model

import com.google.gson.annotations.SerializedName

/**
 * API 공통 응답 형식
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: T?,

    @SerializedName("errorCode")
    val errorCode: String? = null
) {
    fun toResult(): Result<T> {
        return if (success && data != null) {
            Result.success(data)
        } else {
            Result.failure(Exception(message))
        }
    }
}