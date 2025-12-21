package com.project.stampy.data.repository

import android.util.Log
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

    companion object {
        private const val TAG = "AuthRepository"
    }

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

            // needNickname이 false면 이미 닉네임이 있음
            if (!data.needNickname && data.nickname != null) {
                tokenManager.saveNickname(data.nickname)
                Log.d(TAG, "소셜 로그인 성공 - 닉네임: ${data.nickname}")
            } else {
                Log.d(TAG, "소셜 로그인 성공 - 닉네임 설정 필요")
            }

            Log.d(TAG, "userId: ${data.userId}, needNickname: ${data.needNickname}")
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
            Log.d(TAG, "토큰 갱신 성공")
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
            Log.d(TAG, "닉네임 설정 성공: $nickname")
        }
    }

    /**
     * 로그아웃
     *
     * 주의: Refresh Token은 일회용입니다!
     * - 토큰 갱신 시 새로운 Refresh Token이 발급되고 기존 토큰은 무효화됩니다
     * - 403/401 에러 시 토큰을 갱신하고 새 Refresh Token으로 재시도합니다
     */
    suspend fun logout(allDevices: Boolean = false): Result<LogoutResponse> {
        var currentRefreshToken = tokenManager.getRefreshToken()

        // refreshToken이 없으면 로컬만 삭제
        if (currentRefreshToken == null) {
            Log.w(TAG, "Refresh Token 없음 - 로컬 토큰만 삭제")
            tokenManager.clearTokens()
            return Result.failure(Exception("Refresh Token이 없습니다"))
        }

        var token = "Bearer ${tokenManager.getAccessToken()}"

        return try {
            // 첫 번째 시도
            var request = LogoutRequest(currentRefreshToken, allDevices)
            var response = authApi.logout(token, request)

            // 403 또는 401이면 토큰 갱신 후 재시도
            if (response.code() == 403 || response.code() == 401) {
                Log.w(TAG, "로그아웃 실패 (${response.code()}) - 토큰 갱신 시도")

                // 토큰 갱신 (새로운 Access Token과 Refresh Token을 받음)
                val refreshResult = refreshToken()

                if (refreshResult.isSuccess) {
                    Log.d(TAG, "토큰 갱신 성공 - 로그아웃 재시도")

                    // 토큰 갱신 후 새로운 Refresh Token 사용
                    token = "Bearer ${tokenManager.getAccessToken()}"
                    currentRefreshToken = tokenManager.getRefreshToken()!!

                    // 새 Refresh Token으로 request 재생성
                    request = LogoutRequest(currentRefreshToken, allDevices)
                    response = authApi.logout(token, request)

                    Log.d(TAG, "재시도 결과: ${response.code()}")
                } else {
                    Log.e(TAG, "토큰 갱신 실패 - 로컬 토큰만 삭제")
                    tokenManager.clearTokens()
                    return Result.failure(Exception("토큰 갱신 실패"))
                }
            }

            // 최종 결과 처리
            val result = if (response.isSuccessful) {
                Log.d(TAG, "로그아웃 API 성공")
                response.body()?.toResult()
                    ?: Result.failure(Exception("응답 없음"))
            } else {
                Log.e(TAG, "로그아웃 API 실패: ${response.code()}")
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }

            // 성공/실패 여부와 관계없이 로컬 토큰 삭제
            tokenManager.clearTokens()
            Log.d(TAG, "로컬 토큰 삭제 완료")

            result
        } catch (e: Exception) {
            // 예외 발생해도 로컬 토큰 삭제
            Log.e(TAG, "로그아웃 API 호출 실패: ${e.message}")
            tokenManager.clearTokens()
            Log.d(TAG, "로컬 토큰 삭제 완료 (예외 발생)")
            Result.failure(e)
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
            Log.d(TAG, "회원탈퇴 성공 - 로컬 토큰 삭제")
        }
    }

    /**
     * 로그인 상태 확인
     */
    fun isLoggedIn(): Boolean {
        val isLoggedIn = tokenManager.isLoggedIn()
        Log.d(TAG, "로그인 상태 확인: $isLoggedIn")
        return isLoggedIn
    }
}