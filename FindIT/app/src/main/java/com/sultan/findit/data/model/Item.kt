package com.sultan.findit.data.model

import com.google.gson.annotations.SerializedName

data class Item(
    val id: Int,

    @SerializedName("category_id")
    val categoryId: Int?,

    val category: Category?,

    val name: String,
    val description: String,
    val photo: String?,

    @SerializedName("photo_url")
    val photoUrl: String?,

    @SerializedName("claim_photo_url")
    val claimPhotoUrl: String? = null,

    @SerializedName("found_location")
    val foundLocation: String,

    @SerializedName("found_date")
    val foundDate: String,

    @SerializedName("pickup_location")
    val pickupLocation: String,

    val characteristics: String?,

    @SerializedName("admin_note")
    val adminNote: String?,

    val status: String,

    @SerializedName("published_at")
    val publishedAt: String? = null,

    @SerializedName("closed_at")
    val closedAt: String? = null
)