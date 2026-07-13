package com.sultan.findit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey
    val id: Int,

    val categoryId: Int?,
    val categoryName: String?,

    val name: String,
    val description: String,

    val photoUrl: String?,

    val claimPhotoUrl: String?,

    val foundLocation: String,
    val foundDate: String,
    val pickupLocation: String,

    val characteristics: String?,
    val adminNote: String?,

    val status: String
)