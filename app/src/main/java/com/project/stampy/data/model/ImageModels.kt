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
    val contentType: String
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