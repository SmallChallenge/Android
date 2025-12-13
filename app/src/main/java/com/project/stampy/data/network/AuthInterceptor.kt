package com.project.stampy.data.network

import com.project.stampy.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * API 요청에 JWT 토큰 자동으로 추가하는 인터셉터
 */
class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 로그인/회원가입 API는 토큰 불필요
        if (originalRequest.url.encodedPath.contains("/auth/social-login") ||
            originalRequest.url.encodedPath.contains("/auth/refresh")) {
            return chain.proceed(originalRequest)
        }

        // Access Token 가져오기
        val accessToken = tokenManager.getAccessToken()

        // 토큰이 없으면 원래 요청 그대로 진행
        if (accessToken.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Authorization 헤더에 토큰 추가
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(newRequest)
    }
}