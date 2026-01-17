package com.project.stampy.data.model

import com.google.gson.annotations.SerializedName

/**
 * 사용자 차단 요청
 */
data class BlockUserRequest(
    @SerializedName("nickname")
    val nickname: String
)

/**
 * 사용자 차단 응답
 */
data class BlockUserResponse(
    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("blockedAt")
    val blockedAt: String
)