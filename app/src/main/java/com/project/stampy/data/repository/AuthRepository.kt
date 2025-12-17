package com.project.stampy.data.repository

import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.model.*
import com.project.stampy.data.network.AuthApiService
import com.project.stampy.data.network.RetrofitClient
import retrofit2.Response

/**
 * 인증 관련 Repository
 */
class AuthRepository(
    private val tokenManager: TokenManager
) {
    private val authApi: AuthApiService =
        RetrofitClient.createService(AuthApiService::class.java)

    /**
     * 공통 API(서버 오류) 호출 처리 함수
     */
    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<ApiResponse<T>>
    ): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.toResult()
                    ?: Result.failure(Exception("응답 없음"))
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 소셜 로그인
     */
    suspend fun socialLogin(
        socialType: String,
        accessToken: String
    ): Result<SocialLoginResponse> {
        val request = SocialLoginRequest(socialType, accessToken)
        return safeApiCall { authApi.socialLogin(request) }.onSuccess { data ->
            // 토큰 저장
            tokenManager.saveAccessToken(data.accessToken)
            tokenManager.saveRefreshToken(data.refreshToken)
            tokenManager.saveUserId(data.userId)
        }
    }

    /**
     * 토큰 갱신
     */
    suspend fun refreshToken(): Result<RefreshTokenResponse> {
        val refreshToken = tokenManager.getRefreshToken()
            ?: return Result.failure(Exception("Refresh Token이 없습니다"))

        val request = RefreshTokenRequest(refreshToken)
        return safeApiCall { authApi.refreshToken(request) }.onSuccess { data ->
            // 새 토큰 저장
            tokenManager.saveAccessToken(data.accessToken)
            tokenManager.saveRefreshToken(data.refreshToken)
            tokenManager.saveNickname(data.nickname)
        }
    }

    /**
     * 닉네임 설정
     */
    suspend fun setNickname(nickname: String): Result<NicknameResponse> {
        val token = "Bearer ${tokenManager.getAccessToken()}"
        val request = NicknameRequest(nickname)
        return safeApiCall { authApi.setNickname(token, request) }.onSuccess {
            tokenManager.saveNickname(nickname)
        }
    }

    /**
     * 로그아웃
     */
    suspend fun logout(allDevices: Boolean = false): Result<LogoutResponse> {
        val token = "Bearer ${tokenManager.getAccessToken()}"
        val refreshToken = tokenManager.getRefreshToken()
            ?: return Result.failure(Exception("Refresh Token이 없습니다"))

        val request = LogoutRequest(refreshToken, allDevices)
        return safeApiCall { authApi.logout(token, request) }.onSuccess {
            // 로컬 토큰 삭제
            tokenManager.clearTokens()
        }
    }

    /**
     * 회원탈퇴
     */
    suspend fun withdrawal(): Result<WithdrawalResponse> {
        val token = "Bearer ${tokenManager.getAccessToken()}"
        val refreshToken = tokenManager.getRefreshToken()
            ?: return Result.failure(Exception("Refresh Token이 없습니다"))

        val request = WithdrawalRequest(refreshToken)
        return safeApiCall { authApi.withdrawal(token, request) }.onSuccess {
            // 로컬 토큰 삭제
            tokenManager.clearTokens()
        }
    }

    /**
     * 로그인 상태 확인
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
}