package com.project.stampy.data.model

/**
 * 사진 메타데이터
 * 로컬에 저장된 사진의 추가 정보를 관리
 */
data class PhotoMetadata(
    val fileName: String,            // 파일명
    val category: String,            // 카테고리: STUDY, EXERCISE, FOOD, ETC
    val visibility: String,          // 공개여부: PUBLIC, PRIVATE
    val createdAt: Long,             // 촬영 시간 (timestamp)
    val uploadedAt: Long = System.currentTimeMillis(),  // 업로드 시간(정렬용)
    val templateName: String? = null, // 적용된 템플릿 이름
    val isServerUploaded: Boolean = false, // 서버 업로드 여부
    val imageId: Long? = null
)