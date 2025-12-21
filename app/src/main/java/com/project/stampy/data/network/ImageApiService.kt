package com.project.stampy.data.network

import com.project.stampy.data.model.*
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * 이미지 관련 API 서비스
 */
interface ImageApiService {

    /**
     * 이미지 저장 (메타데이터 저장)
     */
    @POST("/api/v1/images/save")
    suspend fun saveImage(
        @Header("Authorization") token: String,
        @Body request: ImageSaveRequest
    ): Response<ApiResponse<ImageSaveResponse>>

    /**
     * Presigned URL 생성
     */
    @POST("/api/v1/images/presigned-url")
    suspend fun getPresignedUrl(
        @Header("Authorization") token: String,
        @Body request: PresignedUrlRequest
    ): Response<ApiResponse<PresignedUrlResponse>>

    /**
     * S3에 이미지 업로드 (Presigned URL 사용)
     */
    @PUT
    suspend fun uploadToS3(
        @Url url: String,
        @Header("Content-Type") contentType: String,
        @Body file: RequestBody
    ): Response<Unit>

    /**
     * 내 앨범 리스트 조회
     */
    @GET("/api/v1/images")
    suspend fun getMyImages(
        @Header("Authorization") token: String,
        @Query("category") category: String? = null, // EXERCISE, FOOD, STUDY
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<ImageListResponse>>

    /**
     * 이미지 상세 조회
     */
    @GET("/api/v1/images/{imageId}")
    suspend fun getImageDetail(
        @Header("Authorization") token: String,
        @Path("imageId") imageId: Long
    ): Response<ApiResponse<ImageDetailResponse>>

    /**
     * 이미지 수정
     */
    @PATCH("/api/v1/images/{imageId}")
    suspend fun updateImage(
        @Header("Authorization") token: String,
        @Path("imageId") imageId: Long,
        @Body request: ImageUpdateRequest
    ): Response<ApiResponse<ImageDetailResponse>>

    /**
     * 이미지 삭제
     */
    @DELETE("/api/v1/images/{imageId}")
    suspend fun deleteImage(
        @Header("Authorization") token: String,
        @Path("imageId") imageId: Long
    ): Response<ApiResponse<ImageDeleteResponse>>
}