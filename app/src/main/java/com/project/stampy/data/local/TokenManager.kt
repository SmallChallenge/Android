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

        // 만료 5분 전에 갱신 (밀리초)
        private const val TOKEN_REFRESH_THRESHOLD = 5 * 60 * 1000L
    }

    /**
     * Access Token 저장 (만료 시간 포함)
     * @param token Access Token
     * @param expiresIn 토큰 유효 시간 (초 단위) - 서버에서 받은 값
     */
    fun saveAccessToken(token: String, expiresIn: Long = 3600) {
        val expireAt = System.currentTimeMillis() + (expiresIn * 1000)
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, token)
            .putLong(KEY_ACCESS_TOKEN_EXPIRE_AT, expireAt)
            .apply()

        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        Log.d(TAG, "Access Token 저장 완료")
        Log.d(TAG, "  - 현재 시간: ${formatter.format(System.currentTimeMillis())}")
        Log.d(TAG, "  - 만료 시간: ${formatter.format(expireAt)}")
        Log.d(TAG, "  - 유효 시간: ${expiresIn}초")
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
     * Access Token이 만료되었는지 체크
     */
    fun isAccessTokenExpired(): Boolean {
        val expireAt = getAccessTokenExpireAt()
        if (expireAt == 0L) return true

        val now = System.currentTimeMillis()
        return now >= expireAt
    }

    /**
     * Access Token이 곧 만료되는지 체크
     * @return true: 만료 임박 (5분 이내) 또는 이미 만료됨, false: 아직 여유 있음
     */
    fun isAccessTokenExpiringSoon(): Boolean {
        val expireAt = getAccessTokenExpireAt()
        // 만료 시간 정보 없으면 갱신 필요
        if (expireAt == 0L) {
            Log.d(TAG, "만료 시간 정보 없음 - 갱신 필요")
            return true
        }

        val now = System.currentTimeMillis()
        val timeLeft = expireAt - now

        val isExpiring = timeLeft <= TOKEN_REFRESH_THRESHOLD

        if (isExpiring) {
            if (timeLeft < 0) {
                Log.w(TAG, "Access Token 이미 만료됨 - ${-timeLeft / 1000}초 전에 만료")
            } else {
                Log.d(TAG, "Access Token 만료 임박 - 남은 시간: ${timeLeft / 1000}초")
            }
        }

        return isExpiring
    }

    /**
     * Refresh Token 저장
     */
    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
        Log.d(TAG, "Refresh Token 저장 완료")
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
        val hasTokens = getAccessToken() != null && getRefreshToken() != null

        if (!hasTokens) {
            return false
        }

        // 토큰이 있어도 완전히 만료되었으면 로그인 상태 아님
        if (isAccessTokenExpired()) {
            Log.w(TAG, "토큰이 완전히 만료됨 - 재로그인 필요")
            return false
        }

        return true
    }

    /**
     * 모든 토큰 삭제 (로그아웃)
     */
    fun clearTokens() {
        prefs.edit().clear().apply()
        Log.d(TAG, "모든 토큰 삭제됨")
    }
}