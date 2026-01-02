package com.project.stampy.data.repository

import com.project.stampy.data.local.TokenManager
import com.project.stampy.data.model.*
import com.project.stampy.data.network.CommunityApiService
import com.project.stampy.data.network.RetrofitClient
import retrofit2.Response

/**
 * 커뮤니티 관련 Repository
 */
class CommunityRepository(
    private val tokenManager: TokenManager
) {
    private val communityApi: CommunityApiService =
        RetrofitClient.createService(CommunityApiService::class.java)

    /**
     * 공통 API 호출 처리
     */
    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<ApiResponse<T>>
    ): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.toResult()
                    ?: Result.failure(Exception("응답 없음"))
            } else {
                Result.failure(Exception("서버 오류: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 커뮤니티 피드 조회
     */
    suspend fun getCommunityFeeds(
        category: String? = null,
        lastPublishedAt: String? = null,
        lastImageId: Long? = null,
        size: Int = 20,
        sort: String = "LATEST"
    ): Result<CommunityFeedResponse> {
        val token = "Bearer ${tokenManager.getAccessToken()}"
        return safeApiCall {
            communityApi.getCommunityFeeds(
                token = token,
                category = category,
                lastPublishedAt = lastPublishedAt,
                lastImageId = lastImageId,
                size = size,
                sort = sort
            )
        }
    }

    /**
     * 좋아요 토글
     */
    suspend fun toggleLike(imageId: Long): Result<LikeToggleResponse> {
        val token = "Bearer ${tokenManager.getAccessToken()}"
        return safeApiCall {
            communityApi.toggleLike(token, imageId)
        }
    }

    /**
     * 게시물 신고
     */
    suspend fun reportPost(imageId: Long): Result<ReportResponse> {
        val token = "Bearer ${tokenManager.getAccessToken()}"
        return safeApiCall {
            communityApi.reportPost(token, imageId)
        }
    }
}