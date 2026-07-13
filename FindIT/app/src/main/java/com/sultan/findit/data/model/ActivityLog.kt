package com.sultan.findit.data.model

import com.google.gson.annotations.SerializedName

data class ActivityLog(
    val id: Long,
    @SerializedName("user_id") val userId: Long,
    val action: String,
    val description: String,
    @SerializedName("ip_address") val ipAddress: String?,
    @SerializedName("user_agent") val userAgent: String?,
    @SerializedName("created_at") val createdAt: String
)