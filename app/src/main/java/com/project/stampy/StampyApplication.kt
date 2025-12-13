package com.project.stampy

import android.app.Application
import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.network.RetrofitClient

/**
 * Application 클래스
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
    }
}