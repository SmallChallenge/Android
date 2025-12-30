package com.project.stampy.data.network

import com.project.stampy.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * 인증 관련 API 서비스
 */
interface AuthApiService {

    /**
     * 소셜 로그인
     */
    @POST("/api/v1/auth/social-login")
    suspend fun socialLogin(
        @Body request: SocialLoginRequest
    ): Response<ApiResponse<SocialLoginResponse>>

    /**
     * 약관 동의
     */
    @POST("/api/v1/auth/terms-agreement")
    suspend fun agreeTerms(
        @Header("Authorization") token: String,
        @Body request: TermsAgreementRequest
    ): Response<ApiResponse<TermsAgreementResponse>>

    /**
     * 토큰 갱신
     */
    @POST("/api/v1/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<ApiResponse<RefreshTokenResponse>>

    /**
     * 닉네임 설정
     */
    @POST("/api/v1/auth/nickname")
    suspend fun setNickname(
        @Header("Authorization") token: String,
        @Body request: NicknameRequest
    ): Response<ApiResponse<NicknameResponse>>

    /**
     * 로그아웃
     */
    @POST("/api/v1/auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String,
        @Body request: LogoutRequest
    ): Response<ApiResponse<LogoutResponse>>

    /**
     * 회원탈퇴
     */
    @POST("/api/v1/auth/cancel-registration")
    suspend fun withdrawal(
        @Header("Authorization") token: String,
        @Body request: WithdrawalRequest
    ): Response<ApiResponse<WithdrawalResponse>>
}