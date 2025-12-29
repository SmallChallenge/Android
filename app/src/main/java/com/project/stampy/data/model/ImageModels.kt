package com.project.stampy.data.model

import com.google.gson.annotations.SerializedName

/**
 * 이미지 저장 요청
 */
data class ImageSaveRequest(
    @SerializedName("originalFilename")
    val originalFilename: String,

    @SerializedName("objectKey")
    val objectKey: String,

    @SerializedName("contentType")
    val contentType: String,

    @SerializedName("fileSize")
    val fileSize: Long,

    @SerializedName("category")
    val category: String, // "STUDY", "EXERCISE", "FOOD", "ETC"

    @SerializedName("visibility")
    val visibility: String = "PRIVATE" // "PRIVATE", "PUBLIC"
)

/**
 * 이미지 저장 응답
 */
data class ImageSaveResponse(
    @SerializedName("imageId")
    val imageId: Long,

    @SerializedName("originalFilename")
    val originalFilename: String,

    @SerializedName("objectKey")
    val objectKey: String,

    @SerializedName("fileUrl")
    val fileUrl: String,

    @SerializedName("fileSize")
    val fileSize: Long,

    @SerializedName("contentType")
    val contentType: String,

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("savedAt")
    val savedAt: String,

    @SerializedName("timestamp")
    val timestamp: String
)

/**
 * Presigned URL 요청
 */
data class PresignedUrlRequest(
    @SerializedName("filename")
    val filename: String,

    @SerializedName("contentType")
    val contentType: String,

    @SerializedName("fileSize")
    val fileSize: Long
)

/**
 * Presigned URL 응답
 */
data class PresignedUrlResponse(
    @SerializedName("uploadUrl")
    val uploadUrl: String,

    @SerializedName("objectKey")
    val objectKey: String,

    @SerializedName("expireAt")
    val expireAt: String,

    @SerializedName("generatedAt")
    val generatedAt: String
)

/**
 * 이미지 리스트 조회 응답
 */
data class ImageListResponse(
    @SerializedName("images")
    val images: List<ImageItem>,

    @SerializedName("pageInfo")
    val pageInfo: PageInfo
)

/**
 * 페이지 정보
 */
data class PageInfo(
    @SerializedName("currentPage")
    val currentPage: Int,

    @SerializedName("pageSize")
    val pageSize: Int,

    @SerializedName("totalElements")
    val totalElements: Int,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("first")
    val first: Boolean,

    @SerializedName("last")
    val last: Boolean,

    @SerializedName("hasNext")
    val hasNext: Boolean,

    @SerializedName("hasPrevious")
    val hasPrevious: Boolean
)

/**
 * 이미지 아이템
 */
data class ImageItem(
    @SerializedName("imageId")
    val imageId: Long,

    @SerializedName("accessUrl")
    val accessUrl: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("visibility")
    val visibility: String,

    @SerializedName("originalTakenAt")
    val originalTakenAt: String
)

/**
 * 이미지 상세 조회 응답
 */
data class ImageDetailResponse(
    @SerializedName("imageId")
    val imageId: Long,

    @SerializedName("userId")
    val userId: Long,

    @SerializedName("originalFilename")
    val originalFilename: String,

    @SerializedName("objectKey")
    val objectKey: String,

    @SerializedName("accessUrl")
    val accessUrl: String,

    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String?,

    @SerializedName("category")
    val category: String,

    @SerializedName("visibility")
    val visibility: String,

    @SerializedName("fileSize")
    val fileSize: Long,

    @SerializedName("contentType")
    val contentType: String,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)

/**
 * 이미지 수정 요청
 */
data class ImageUpdateRequest(
    @SerializedName("category")
    val category: String?, // "EXERCISE", "FOOD", "STUDY"

    @SerializedName("visibility")
    val visibility: String? // "PRIVATE", "PUBLIC"
)

/**
 * 이미지 삭제 응답
 */
data class ImageDeleteResponse(
    @SerializedName("imageId")
    val imageId: Long,

    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("deletedAt")
    val deletedAt: String
)