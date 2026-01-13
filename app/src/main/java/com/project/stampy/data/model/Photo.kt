package com.project.stampy.data.model

import java.io.File

/**
 * 사진 데이터 모델
 */
data class Photo(
    val file: File,
    val timestamp: Long = file.lastModified(),
    val category: String = "전체",    // 나중에 카테고리 기능 추가 시 사용
    val serverUrl: String? = null,   // 서버 URL (로그인 사용자)
    val imageId: Long? = null,       // 서버 이미지 ID
    val visibility: String? = null   // 공개 여부
)