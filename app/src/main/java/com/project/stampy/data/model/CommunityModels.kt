package com.project.stampy.data.model

import com.google.gson.annotations.SerializedName

/**
 * 커뮤니티 피드 응답
 */
data class CommunityFeedResponse(
    @SerializedName("feeds")
    val feeds: List<FeedItem>,

    @SerializedName("sliceInfo")
    val sliceInfo: SliceInfo
)

/**
 * 피드 아이템
 */
data class FeedItem(
    @SerializedName("imageId")
    val imageId: Long,

    @SerializedName("accessUrl")
    val accessUrl: String,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String?,

    @SerializedName("liked")
    val isLiked: Boolean,

    @SerializedName("likeCount")
    val likeCount: Int,

    @SerializedName("publishedAt")
    val publishedAt: String
)

/**
 * 슬라이스 정보 (커서 기반 페이징)
 */
data class SliceInfo(
    @SerializedName("hasNext")
    val hasNext: Boolean,

    @SerializedName("nextCursorPublishedAt")
    val nextCursorPublishedAt: String?,

    @SerializedName("nextCursorId")
    val nextCursorId: Long?
)

/**
 * 좋아요 토글 응답
 */
data class LikeToggleResponse(
    @SerializedName("liked")
    val isLiked: Boolean
)

/**
 * 신고 응답
 */
data class ReportResponse(
    @SerializedName("imageId")
    val imageId: Long,

    @SerializedName("reportedAt")
    val reportedAt: String
)