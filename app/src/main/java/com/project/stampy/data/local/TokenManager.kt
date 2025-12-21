package com.project.stampy.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * JWT 토큰 관리 클래스
 */
class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "TokenManager"
        private const val PREFS_NAME = "stampy_token_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_ACCESS_TOKEN_EXPIRE_AT = "access_token_expire_at"

        // Access Token 유효기간: 1시간 (밀리초)
        private const val ACCESS_TOKEN_VALIDITY = 60 * 60 * 1000L

        // 만료 5분 전에 갱신 (밀리초)
        private const val TOKEN_REFRESH_THRESHOLD = 5 * 60 * 1000L
    }

    /**
     * Access Token 저장
     */
    fun saveAccessToken(token: String) {
        val expireAt = System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putLong(KEY_ACCESS_TOKEN_EXPIRE_AT, expireAt)
            .apply()

        Log.d(TAG, "Access Token 저장 - 만료 시간: ${java.text.SimpleDateFormat("HH:mm:ss").format(expireAt)}")
    }

    /**
     * Access Token 가져오기
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Access Token 만료 시간 가져오기
     */
    fun getAccessTokenExpireAt(): Long {
        return prefs.getLong(KEY_ACCESS_TOKEN_EXPIRE_AT, 0L)
    }

    /**
     * Access Token이 곧 만료되는지 체크
     * @return true: 만료 임박 (5분 이내), false: 아직 여유 있음
     */
    fun isAccessTokenExpiringSoon(): Boolean {
        val expireAt = getAccessTokenExpireAt()
        if (expireAt == 0L) return true // 만료 시간 정보 없으면 갱신 필요

        val now = System.currentTimeMillis()
        val timeLeft = expireAt - now

        val isExpiring = timeLeft <= TOKEN_REFRESH_THRESHOLD

        if (isExpiring) {
            Log.d(TAG, "Access Token 만료 임박 - 남은 시간: ${timeLeft / 1000}초")
        }

        return isExpiring
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
        Log.d(TAG, "모든 토큰 삭제됨")
    }
}