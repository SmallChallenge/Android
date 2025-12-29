package com.project.stampy.data.model

import java.io.File

/**
 * 사진 메타데이터
 * 로컬에 저장된 사진의 추가 정보를 관리
 */

data class PhotoMetadata(
    val fileName: String,            // 파일명
    val category: String,            // 카테고리: STUDY, EXERCISE, FOOD, ETC
    val visibility: String,          // 공개여부: PUBLIC, PRIVATE
    val createdAt: Long,             // 생성 시간 (timestamp)
    val templateName: String? = null, // 적용된 템플릿 이름
    val isServerUploaded: Boolean = false // 서버 업로드 여부
)