package com.project.stampy.data.model

import java.io.File

/**
 * 사진 데이터 모델
 */
data class Photo(
    val file: File,
    val timestamp: Long = file.lastModified(),
    val category: String = "전체" // 나중에 카테고리 기능 추가 시 사용
)