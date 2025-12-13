package com.project.stampy.data.repository

import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.model.*
import com.project.stampy.data.network.AuthApiService
import com.project.stampy.data.network.RetrofitClient

/**
 * 인증 관련 Repository
 */
class AuthRepository(
    private val tokenManager: TokenManager
) {
    private val authApi: AuthApiService =
        RetrofitClient.createService(AuthApiService::class.java)

    /**
     * 소셜 로그인
     */
    suspend fun socialLogin(
        socialType: String,
        accessToken: String
    ): Result<SocialLoginResponse> {
        return try {
            val request = SocialLoginRequest(socialType, accessToken)
            val response = authApi.socialLogin(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    // 토큰 저장
                    tokenManager.saveAccessToken(body.data.accessToken)
                    tokenManager.saveRefreshToken(body.data.refreshToken)
                    tokenManager.saveUserId(body.data.userId)

                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "로그인 실패"))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 토큰 갱신
     */
    suspend fun refreshToken(): Result<RefreshTokenResponse> {
        return try {
            val refreshToken = tokenManager.getRefreshToken()
                ?: return Result.failure(Exception("Refresh Token이 없습니다"))

            val request = RefreshTokenRequest(refreshToken)
            val response = authApi.refreshToken(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    // 새 토큰 저장
                    tokenManager.saveAccessToken(body.data.accessToken)
                    tokenManager.saveRefreshToken(body.data.refreshToken)
                    tokenManager.saveNickname(body.data.nickname)

                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "토큰 갱신 실패"))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 닉네임 설정
     */
    suspend fun setNickname(nickname: String): Result<NicknameResponse> {
        return try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            val request = NicknameRequest(nickname)
            val response = authApi.setNickname(token, request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    tokenManager.saveNickname(nickname)
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "닉네임 설정 실패"))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 로그아웃
     */
    suspend fun logout(allDevices: Boolean = false): Result<LogoutResponse> {
        return try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            val refreshToken = tokenManager.getRefreshToken()
                ?: return Result.failure(Exception("Refresh Token이 없습니다"))

            val request = LogoutRequest(refreshToken, allDevices)
            val response = authApi.logout(token, request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    // 로컬 토큰 삭제
                    tokenManager.clearTokens()
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "로그아웃 실패"))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 회원탈퇴
     */
    suspend fun withdrawal(): Result<WithdrawalResponse> {
        return try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            val refreshToken = tokenManager.getRefreshToken()
                ?: return Result.failure(Exception("Refresh Token이 없습니다"))

            val request = WithdrawalRequest(refreshToken)
            val response = authApi.withdrawal(token, request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    // 로컬 토큰 삭제
                    tokenManager.clearTokens()
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "회원탈퇴 실패"))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 로그인 상태 확인
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
}