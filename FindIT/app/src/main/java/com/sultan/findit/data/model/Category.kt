package com.sultan.findit.data.model

import com.google.gson.annotations.SerializedName

data class Category(
    val id: Int,
    val name: String,

    @SerializedName("items_count")
    val itemsCount: Int? = null
)