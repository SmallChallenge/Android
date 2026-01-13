package com.project.stampy.data.network

import com.project.stampy.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 클라이언트 싱글톤
 */
object RetrofitClient {

    private const val CONNECT_TIMEOUT = 30L
    private const val READ_TIMEOUT = 30L
    private const val WRITE_TIMEOUT = 30L

    private var tokenManager: TokenManager? = null

    /**
     * TokenManager 초기화
     */
    fun initialize(tokenManager: TokenManager) {
        this.tokenManager = tokenManager
    }

    /**
     * OkHttpClient 생성 (일반 API용)
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .apply {
                tokenManager?.let { tm ->
                    // Interceptor: 모든 요청에 Authorization 헤더 추가
                    addInterceptor(AuthInterceptor(tm))

                    // Authenticator: 401 에러 시 자동으로 토큰 갱신
                    authenticator(TokenAuthenticator(tm))
                }
            }
            .build()
    }

    /**
     * S3 전용 OkHttpClient 생성 (최소 헤더, iOS와 동일)
     */
    private fun createS3OkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            // ⚠️ CRITICAL: S3 업로드에는 인터셉터/인증 제거!
            // iOS처럼 최소 헤더만 사용
            .build()
    }

    /**
     * Retrofit 인스턴스 (일반 API용)
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.stampy.kr/")
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * S3 전용 Retrofit 인스턴스
     */
    val s3Retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://kr.object.ncloudstorage.com/")  // S3 base URL
            .client(createS3OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * API 서비스 생성
     */
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }

    /**
     * S3 전용 API 서비스 생성
     */
    fun <T> createS3Service(serviceClass: Class<T>): T {
        return s3Retrofit.create(serviceClass)
    }
}