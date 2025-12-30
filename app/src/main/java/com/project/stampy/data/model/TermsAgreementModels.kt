package com.project.stampy.data.model

import com.google.gson.annotations.SerializedName

/**
 * 약관 동의 요청
 */
data class TermsAgreementRequest(
    @SerializedName("agreedToPrivacyPolicy")
    val agreedToPrivacyPolicy: Boolean,

    @SerializedName("agreedToTermsOfService")
    val agreedToTermsOfService: Boolean,

    @SerializedName("agreedToMarketing")
    val agreedToMarketing: Boolean,

    @SerializedName("allRequiredTermsAgreed")
    val allRequiredTermsAgreed: Boolean
)

/**
 * 약관 동의 응답
 */
data class TermsAgreementResponse(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("userStatus")
    val userStatus: String,  // "ACTIVE"

    @SerializedName("completedAt")
    val completedAt: String  // "2024-01-15T14:30:00"
)