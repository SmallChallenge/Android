package com.project.stampy.data.repository

import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.model.*
import com.project.stampy.data.network.ImageApiService
import com.project.stampy.data.network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * 이미지 관련 Repository
 */
class ImageRepository(
    private val tokenManager: TokenManager
) {
    private val imageApi: ImageApiService =
        RetrofitClient.createService(ImageApiService::class.java)

    /**
     * 이미지 업로드 (전체 프로세스)
     * 1. Presigned URL 생성
     * 2. S3에 업로드
     * 3. 메타데이터 저장
     */
    suspend fun uploadImage(
        imageFile: File,
        category: String = "EXERCISE", // "EXERCISE", "FOOD", "STUDY"
        visibility: String = "PRIVATE", // "PRIVATE", "PUBLIC"
        contentType: String = "image/jpeg"
    ): Result<ImageSaveResponse> {
        return try {
            val token = "Bearer ${tokenManager.getAccessToken()}"

            // 1. Presigned URL 요청
            val presignedRequest = PresignedUrlRequest(
                filename = imageFile.name,
                contentType = contentType,
                fileSize = imageFile.length()
            )

            val presignedResponse = imageApi.getPresignedUrl(token, presignedRequest)

            if (!presignedResponse.isSuccessful || presignedResponse.body()?.data == null) {
                return Result.failure(Exception("Presigned URL 생성 실패"))
            }

            val presignedData = presignedResponse.body()!!.data!!

            // 2. S3에 이미지 업로드
            val requestBody = imageFile.asRequestBody(contentType.toMediaType())
            val uploadResponse = imageApi.uploadToS3(
                url = presignedData.uploadUrl,
                contentType = contentType,
                file = requestBody
            )

            if (!uploadResponse.isSuccessful) {
                return Result.failure(Exception("S3 업로드 실패"))
            }

            // 3. 이미지 메타데이터 저장
            val saveRequest = ImageSaveRequest(
                originalFilename = imageFile.name,
                objectKey = presignedData.objectKey,
                contentType = contentType,
                fileSize = imageFile.length(),
                category = category,
                visibility = visibility
            )

            val saveResponse = imageApi.saveImage(token, saveRequest)

            if (saveResponse.isSuccessful) {
                val body = saveResponse.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "이미지 저장 실패"))
                }
            } else {
                Result.failure(Exception("서버 오류: ${saveResponse.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 내 앨범 리스트 조회
     */
    suspend fun getMyImages(
        category: String? = null, // null이면 전체, "EXERCISE", "FOOD", "STUDY"
        page: Int = 0,
        size: Int = 20
    ): Result<ImageListResponse> {
        return try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            val response = imageApi.getMyImages(token, category, page, size)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "목록 조회 실패"))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 이미지 상세 조회
     */
    suspend fun getImageDetail(imageId: Long): Result<ImageDetailResponse> {
        return try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            val response = imageApi.getImageDetail(token, imageId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "상세 조회 실패"))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 이미지 수정 (카테고리, 공개범위)
     */
    suspend fun updateImage(
        imageId: Long,
        category: String? = null,
        visibility: String? = null
    ): Result<ImageDetailResponse> {
        return try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            val request = ImageUpdateRequest(category, visibility)
            val response = imageApi.updateImage(token, imageId, request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "수정 실패"))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 이미지 삭제 (Soft Delete)
     */
    suspend fun deleteImage(imageId: Long): Result<ImageDeleteResponse> {
        return try {
            val token = "Bearer ${tokenManager.getAccessToken()}"
            val response = imageApi.deleteImage(token, imageId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "삭제 실패"))
                }
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}