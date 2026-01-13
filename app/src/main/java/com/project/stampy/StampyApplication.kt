package com.project.stampy

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.network.RetrofitClient

/**
 * Application 클래스
 *
 * TODO: BuildConfig 이슈 해결 필요
 * - 현재 카카오 앱 키가 하드코딩되어 있음
 * - BuildConfig.KAKAO_APP_KEY로 변경 필요
 * - build.gradle.kts에 설정은 완료되었으나 Sync 문제로 임시 하드코딩
 */
class StampyApplication : Application() {

    lateinit var tokenManager: TokenManager
        private set

    override fun onCreate() {
        super.onCreate()

        // TokenManager 초기화
        tokenManager = TokenManager(this)

        // RetrofitClient 초기화
        RetrofitClient.initialize(tokenManager)

        // Kakao SDK 초기화
        // FIXME: BuildConfig 이슈 해결 후 BuildConfig.KAKAO_APP_KEY로 변경
        KakaoSdk.init(this, "cdcf8640c0483fccee4f1d8d51811081")
    }
}