package com.project.stampy.data.network

import android.util.Log
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * 401 Unauthorized 에러 발생 시 자동으로 토큰을 갱신하는 Authenticator
 *
 * Refresh Token 로테이션 정책:
 * - Refresh Token은 일회용 (사용 시 새로 발급)
 * - 기존 Refresh Token은 즉시 무효화
 */
class TokenAuthenticator(
    private val tokenManager: TokenManager
) : Authenticator {

    companion object {
        private const val TAG = "TokenAuthenticator"
        private const val MAX_RETRY = 3
    }

    override fun authenticate(route: Route?, response: Response): Request? {
        // 이미 3번 시도했으면 포기
        if (responseCount(response) >= MAX_RETRY) {
            Log.e(TAG, "토큰 갱신 최대 재시도 횟수 초과")
            return null
        }

        val refreshToken = tokenManager.getRefreshToken()

        if (refreshToken == null) {
            Log.e(TAG, "Refresh Token 없음 - 재인증 필요")
            // 로그인 화면으로 이동하도록 토큰 삭제
            tokenManager.clearTokens()
            return null
        }

        Log.d(TAG, "401 에러 감지 - 토큰 갱신 시작")

        // 동기적으로 토큰 갱신 (Authenticator는 동기 방식)
        return runBlocking {
            try {
                val authApi = RetrofitClient.createService(AuthApiService::class.java)
                val request = RefreshTokenRequest(refreshToken)
                val refreshResponse = authApi.refreshToken(request)

                if (refreshResponse.isSuccessful && refreshResponse.body()?.success == true) {
                    val newTokens = refreshResponse.body()?.data

                    if (newTokens != null) {
                        // 새로운 Access Token과 Refresh Token 저장
                        tokenManager.saveAccessToken(newTokens.accessToken)
                        tokenManager.saveRefreshToken(newTokens.refreshToken)
                        tokenManager.saveNickname(newTokens.nickname)

                        Log.d(TAG, "토큰 갱신 성공 - 새 토큰으로 재시도")

                        // 새 Access Token으로 요청 재시도
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${newTokens.accessToken}")
                            .build()
                    } else {
                        Log.e(TAG, "토큰 갱신 응답 데이터 없음")
                        tokenManager.clearTokens()
                        null
                    }
                } else {
                    Log.e(TAG, "토큰 갱신 실패: ${refreshResponse.code()}")
                    // Refresh Token도 만료됨 - 재로그인 필요
                    tokenManager.clearTokens()
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "토큰 갱신 중 예외 발생", e)
                tokenManager.clearTokens()
                null
            }
        }
    }

    /**
     * 재시도 횟수 계산
     */
    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse

        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }

        return count
    }
}