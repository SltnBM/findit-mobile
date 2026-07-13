package com.sultan.findit.data.model

import com.google.gson.annotations.SerializedName

data class BasicResponse(
    val success: Boolean,
    val message: String
)

data class ProfileResponse(
    val success: Boolean,
    val message: String,
    val user: User?
)

data class ItemListResponse(
    val success: Boolean,
    val message: String,
    val data: List<Item>,
    val counts: StatusCounts? = null
)

data class SingleItemResponse(
    val success: Boolean,
    val message: String,
    val data: Item?
)

data class CategoryListResponse(
    val success: Boolean,
    val message: String,
    val data: List<Category>
)

data class SingleCategoryResponse(
    val success: Boolean,
    val message: String,
    val data: Category?
)

data class CategoryRequest(
    val name: String
)

data class StatusCounts(
    val draft: Int = 0,
    val published: Int = 0,
    val closed: Int = 0
)

data class ActivityLogListResponse(
    val success: Boolean,
    val message: String,
    val data: List<ActivityLog>
)