package com.project.stampy.data.model

import java.io.File

/**
 * 사진 데이터 모델
 */
data class Photo(
    val file: File,
    val category: String = "전체",
    val serverUrl: String? = null,   // 서버 URL (로그인 사용자)
    val imageId: Long? = null,       // 서버 이미지 ID
    val timestamp: Long = 0L,  // 촬영 시간 (템플릿 표시용)
    val uploadedAt: Long = System.currentTimeMillis(),  // 업로드 시간 (정렬용)
    val visibility: String? = null   // 공개 여부
)