package com.project.stampy.data.local

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.project.stampy.data.model.PhotoMetadata

/**
 * 사진 메타데이터 관리 클래스
 * SharedPreferences에 JSON 형태로 저장
 */
class PhotoMetadataManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val TAG = "PhotoMetadataManager"
        private const val PREFS_NAME = "stampy_photo_metadata_prefs"
        private const val KEY_METADATA_LIST = "metadata_list"
    }

    /**
     * 모든 메타데이터 가져오기
     */
    fun getAllMetadata(): List<PhotoMetadata> {
        val json = prefs.getString(KEY_METADATA_LIST, null) ?: return emptyList()
        val type = object : TypeToken<List<PhotoMetadata>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "메타데이터 파싱 실패", e)
            emptyList()
        }
    }

    /**
     * 메타데이터 저장
     */
    fun saveMetadata(metadata: PhotoMetadata) {
        val currentList = getAllMetadata().toMutableList()
        currentList.add(metadata)
        saveAllMetadata(currentList)
        Log.d(TAG, "메타데이터 저장: ${metadata.fileName}, 카테고리: ${metadata.category}")
    }

    /**
     * 파일명으로 메타데이터 조회
     */
    fun getMetadataByFileName(fileName: String): PhotoMetadata? {
        return getAllMetadata().find { it.fileName == fileName }
    }

    /**
     * 카테고리별 메타데이터 조회
     */
    fun getMetadataByCategory(category: String): List<PhotoMetadata> {
        return getAllMetadata().filter { it.category == category }
    }

    /**
     * 메타데이터 삭제
     */
    fun deleteMetadata(fileName: String) {
        val currentList = getAllMetadata().toMutableList()
        currentList.removeAll { it.fileName == fileName }
        saveAllMetadata(currentList)
        Log.d(TAG, "메타데이터 삭제: $fileName")
    }

    /**
     * 모든 메타데이터 저장
     */
    private fun saveAllMetadata(metadataList: List<PhotoMetadata>) {
        val json = gson.toJson(metadataList)
        prefs.edit().putString(KEY_METADATA_LIST, json).apply()
    }

    /**
     * 서버 업로드 상태 업데이트
     */
    fun updateServerUploadStatus(fileName: String, isUploaded: Boolean) {
        val currentList = getAllMetadata().toMutableList()
        val index = currentList.indexOfFirst { it.fileName == fileName }
        if (index >= 0) {
            currentList[index] = currentList[index].copy(isServerUploaded = isUploaded)
            saveAllMetadata(currentList)
            Log.d(TAG, "서버 업로드 상태 업데이트: $fileName -> $isUploaded")
        }
    }
}