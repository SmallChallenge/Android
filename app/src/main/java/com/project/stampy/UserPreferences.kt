package com.project.stampy.util

import android.content.Context
import android.content.SharedPreferences

/**
 * 사용자 로그인 상태 및 정보 관리
 */
class UserPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "stampy_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PROFILE_IMAGE = "user_profile_image"

        @Volatile
        private var instance: UserPreferences? = null

        fun getInstance(context: Context): UserPreferences {
            return instance ?: synchronized(this) {
                instance ?: UserPreferences(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    /**
     * 로그인 상태 확인
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * 로그인 정보 저장
     */
    fun saveLoginInfo(name: String, email: String, profileImage: String? = null) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_PROFILE_IMAGE, profileImage)
            apply()
        }
    }

    /**
     * 사용자 이름 가져오기
     */
    fun getUserName(): String? {
        return prefs.getString(KEY_USER_NAME, null)
    }

    /**
     * 사용자 이메일 가져오기
     */
    fun getUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    /**
     * 프로필 이미지 URL 가져오기
     */
    fun getProfileImageUrl(): String? {
        return prefs.getString(KEY_USER_PROFILE_IMAGE, null)
    }

    /**
     * 로그아웃 (모든 정보 삭제)
     */
    fun logout() {
        prefs.edit().apply {
            clear()
            apply()
        }
    }
}