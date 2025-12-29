package com.project.stampy.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * 비로그인 유저의 사진 관리 클래스
 * - 비로그인 상태에서 저장한 사진 개수 추적
 * - 최대 20장 제한
 */
class NonLoginPhotoManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "NonLoginPhotoManager"
        private const val PREFS_NAME = "stampy_non_login_photo_prefs"
        private const val KEY_PHOTO_COUNT = "non_login_photo_count"
        private const val MAX_PHOTOS = 20
    }

    /**
     * 현재 저장된 사진 개수 가져오기
     */
    fun getPhotoCount(): Int {
        return prefs.getInt(KEY_PHOTO_COUNT, 0)
    }

    /**
     * 사진 개수 증가
     */
    fun incrementPhotoCount() {
        val currentCount = getPhotoCount()
        prefs.edit().putInt(KEY_PHOTO_COUNT, currentCount + 1).apply()
        Log.d(TAG, "사진 개수 증가: ${currentCount + 1}/${MAX_PHOTOS}")
    }

    /**
     * 사진 개수 감소
     */
    fun decrementPhotoCount() {
        val currentCount = getPhotoCount()
        if (currentCount > 0) {
            prefs.edit().putInt(KEY_PHOTO_COUNT, currentCount - 1).apply()
            Log.d(TAG, "사진 개수 감소: ${currentCount - 1}/${MAX_PHOTOS}")
        }
    }

    /**
     * 사진을 더 저장할 수 있는지 확인
     */
    fun canSaveMorePhotos(): Boolean {
        return getPhotoCount() < MAX_PHOTOS
    }

    /**
     * 최대 저장 가능 개수
     */
    fun getMaxPhotos(): Int {
        return MAX_PHOTOS
    }

    /**
     * 남은 저장 가능 개수
     */
    fun getRemainingPhotos(): Int {
        return MAX_PHOTOS - getPhotoCount()
    }

    /**
     * 카운트 초기화 (로그인 시 호출하지 않음 - 비로그인 사진은 유지)
     */
    fun resetCount() {
        prefs.edit().putInt(KEY_PHOTO_COUNT, 0).apply()
        Log.d(TAG, "사진 개수 초기화")
    }
}