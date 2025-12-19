package com.project.stampy.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * JWT 토큰 관리 클래스
 */
class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "stampy_token_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NICKNAME = "nickname"
    }

    /**
     * Access Token 저장
     */
    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    /**
     * Access Token 가져오기
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Refresh Token 저장
     */
    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    /**
     * Refresh Token 가져오기
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * User ID 저장
     */
    fun saveUserId(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    /**
     * User ID 가져오기
     */
    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1L)
    }

    /**
     * Nickname 저장
     */
    fun saveNickname(nickname: String) {
        prefs.edit().putString(KEY_NICKNAME, nickname).apply()
    }

    /**
     * Nickname 가져오기
     */
    fun getNickname(): String? {
        return prefs.getString(KEY_NICKNAME, null)
    }

    /**
     * 로그인 상태 확인
     */
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null && getRefreshToken() != null
    }

    /**
     * 모든 토큰 삭제 (로그아웃)
     */
    fun clearTokens() {
        prefs.edit().clear().apply()
    }
}