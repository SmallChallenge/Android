package com.project.stampy.data.network

import com.project.stampy.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * 커뮤니티 관련 API 서비스
 */
interface CommunityApiService {

    /**
     * 커뮤니티 피드 조회 (커서 기반 페이징)
     */
    @GET("/api/v1/community/feeds")
    suspend fun getCommunityFeeds(
        @Header("Authorization") token: String,
        @Query("category") category: String? = null,
        @Query("lastPublishedAt") lastPublishedAt: String? = null,
        @Query("lastImageId") lastImageId: Long? = null,
        @Query("size") size: Int = 20,
        @Query("sort") sort: String = "LATEST" // LATEST 최근순, POPULAR 인기순
    ): Response<ApiResponse<CommunityFeedResponse>>

    /**
     * 좋아요 토글
     */
    @POST("/api/v1/community/{imageId}/like")
    suspend fun toggleLike(
        @Header("Authorization") token: String,
        @Path("imageId") imageId: Long
    ): Response<ApiResponse<LikeToggleResponse>>

    /**
     * 게시물 신고
     */
    @POST("/api/v1/community/{imageId}/report")
    suspend fun reportPost(
        @Header("Authorization") token: String,
        @Path("imageId") imageId: Long
    ): Response<ApiResponse<ReportResponse>>
}