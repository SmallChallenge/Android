package com.project.stampy.data.model

import com.google.gson.annotations.SerializedName

/**
 * 소셜 로그인 요청
 */
data class SocialLoginRequest(
    @SerializedName("socialType")
    val socialType: String, // "GOOGLE", "KAKAO"

    @SerializedName("accessToken")
    val accessToken: String
)

/**
 * 소셜 로그인 응답
 */
data class SocialLoginResponse(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("nickname")
    val nickname: String?,

    @SerializedName("socialType")
    val socialType: String,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String?,

    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("isNewUser")
    val isNewUser: Boolean,

    @SerializedName("needNickname")
    val needNickname: Boolean,

    @SerializedName("userStatus")
    val userStatus: String  // "PENDING", "ACTIVE", "WITHDRAWN"
)

/**
 * 토큰 갱신 요청
 */
data class RefreshTokenRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

/**
 * 토큰 갱신 응답
 */
data class RefreshTokenResponse(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("userId")
    val userId: Long,

    @SerializedName("nickname")
    val nickname: String
)

/**
 * 닉네임 설정 요청
 */
data class NicknameRequest(
    @SerializedName("nickname")
    val nickname: String
)

/**
 * 닉네임 설정 응답
 */
data class NicknameResponse(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("isProfileComplete")
    val isProfileComplete: Boolean
)

/**
 * 로그아웃 요청
 */
data class LogoutRequest(
    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("allDevices")
    val allDevices: Boolean = false
)

/**
 * 로그아웃 응답
 */
data class LogoutResponse(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("allDevices")
    val allDevices: Boolean,

    @SerializedName("invalidatedTokenCount")
    val invalidatedTokenCount: Int,

    @SerializedName("logoutTime")
    val logoutTime: String
)

/**
 * 가입 취소 응답 (cancel-registration)
 */
data class CancelRegistrationResponse(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("deletedAt")
    val deletedAt: String
)

/**
 * 회원탈퇴 요청
 */
data class WithdrawalRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

/**
 * 회원탈퇴 응답
 */
data class WithdrawalResponse(
    @SerializedName("userId")
    val userId: Long,

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("maskedNickname")
    val maskedNickname: String,

    @SerializedName("invalidatedTokenCount")
    val invalidatedTokenCount: Int,

    @SerializedName("withdrawalTime")
    val withdrawalTime: String
)