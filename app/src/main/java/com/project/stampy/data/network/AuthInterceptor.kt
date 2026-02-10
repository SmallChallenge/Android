package com.project.stampy.data.network

import android.util.Log
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response

/**
 * API 요청에 JWT 토큰 자동으로 추가하는 인터셉터
 *
 * 사전 토큰 갱신 전략:
 * - API 호출 전에 토큰 만료 시간 체크
 * - 만료 5분 전이면 미리 토큰 갱신
 * - 사용자 경험을 위한 지연 최소화
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
        private val refreshMutex = Mutex()  // 동시 갱신 방지
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 로그인/회원가입 API는 토큰 불필요
        val path = originalRequest.url.encodedPath
        if (path.contains("/auth/social-login") ||
            path.contains("/auth/refresh")) {
            return chain.proceed(originalRequest)
        }

        // 사전 토큰 갱신: 만료 임박하면 미리 갱신
        runBlocking {
            if (tokenManager.isAccessTokenExpiringSoon()) {
                refreshMutex.withLock {
                    if (tokenManager.isAccessTokenExpiringSoon()) {
                        Log.d(TAG, "토큰 만료 임박 감지 - 사전 갱신 시작")
                        refreshTokenProactively()
                    }
                }
            }
        }

        // Access Token 가져오기
        val accessToken = tokenManager.getAccessToken()

        // 토큰이 없으면 원래 요청 그대로 진행
        if (accessToken.isNullOrEmpty()) {
            Log.w(TAG, "Access Token 없음 - 인증 없이 요청")
            return chain.proceed(originalRequest)
        }

        // Authorization 헤더에 토큰 추가
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(newRequest)
    }

    /**
     * 사전에 토큰을 갱신
     */
    private suspend fun refreshTokenProactively() {
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Log.e(TAG, "Refresh Token 없음 - 갱신 불가")
            return
        }

        try {
            val authApi = RetrofitClient.createService(AuthApiService::class.java)
            val request = RefreshTokenRequest(refreshToken)
            val response = authApi.refreshToken(request)

            if (response.isSuccessful && response.body()?.success == true) {
                val newTokens = response.body()?.data

                if (newTokens != null) {
                    // expiresIn 값을 서버에서 받아와야 함
                    val expiresIn = 3600L // 서버 응답에 포함되어야 함

                    tokenManager.saveAccessToken(newTokens.accessToken, expiresIn)
                    tokenManager.saveRefreshToken(newTokens.refreshToken)
                    tokenManager.saveNickname(newTokens.nickname)

                    Log.d(TAG, "사전 토큰 갱신 성공")
                } else {
                    Log.e(TAG, "사전 토큰 갱신 응답 데이터 없음")
                }
            } else {
                Log.e(TAG, "사전 토큰 갱신 실패: ${response.code()}")
                if (response.code() == 401) {
                    Log.e(TAG, "Refresh Token도 만료됨 - 재로그인 필요")
                    tokenManager.clearTokens()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "사전 토큰 갱신 중 예외", e)
        }
    }
}