package com.sultan.findit.data.remote

import com.sultan.findit.data.model.ActivityLogListResponse
import com.sultan.findit.data.model.BasicResponse
import com.sultan.findit.data.model.CategoryListResponse
import com.sultan.findit.data.model.CategoryRequest
import com.sultan.findit.data.model.ItemListResponse
import com.sultan.findit.data.model.LoginRequest
import com.sultan.findit.data.model.LoginResponse
import com.sultan.findit.data.model.ProfileResponse
import com.sultan.findit.data.model.RegisterRequest
import com.sultan.findit.data.model.SingleCategoryResponse
import com.sultan.findit.data.model.SingleItemResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @POST("register")
    suspend fun register(
        @Body request: RegisterRequest
    ): LoginResponse

    @POST("logout")
    suspend fun logout(): BasicResponse

    @GET("profile")
    suspend fun profile(): ProfileResponse

    @GET("items")
    suspend fun getItems(
        @Query("search") search: String? = null,
        @Query("category_id") categoryId: Int? = null,
        @Query("location") location: String? = null,
        @Query("date") date: String? = null
    ): ItemListResponse

    @GET("items/{id}")
    suspend fun getItem(
        @Path("id") id: Int
    ): SingleItemResponse

    @GET("categories")
    suspend fun getCategories(): CategoryListResponse

    @GET("admin/items")
    suspend fun getAdminItems(
        @Query("status") status: String? = null,
        @Query("search") search: String? = null
    ): ItemListResponse

    @GET("admin/items/{id}")
    suspend fun getAdminItem(
        @Path("id") id: Int
    ): SingleItemResponse

    @Multipart
    @POST("admin/items")
    suspend fun createItem(
        @Part("category_id") categoryId: RequestBody,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("found_location") foundLocation: RequestBody,
        @Part("found_date") foundDate: RequestBody,
        @Part("pickup_location") pickupLocation: RequestBody,
        @Part("characteristics") characteristics: RequestBody,
        @Part("admin_note") adminNote: RequestBody
    ): SingleItemResponse

    @Multipart
    @POST("admin/items/{id}")
    suspend fun updateItem(
        @Path("id") id: Int,
        @Part("category_id") categoryId: RequestBody,
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part?,
        @Part("found_location") foundLocation: RequestBody,
        @Part("found_date") foundDate: RequestBody,
        @Part("pickup_location") pickupLocation: RequestBody,
        @Part("characteristics") characteristics: RequestBody,
        @Part("admin_note") adminNote: RequestBody
    ): SingleItemResponse

    @DELETE("admin/items/{id}")
    suspend fun deleteItem(
        @Path("id") id: Int
    ): BasicResponse

    @PUT("admin/items/{id}/publish")
    suspend fun publishItem(
        @Path("id") id: Int
    ): SingleItemResponse

    @Multipart
    @POST("admin/items/{id}/close")
    suspend fun closeItem(
        @Path("id") id: Int,
        @Part claimPhoto: MultipartBody.Part
    ): SingleItemResponse

    @GET("admin/categories")
    suspend fun getAdminCategories(): CategoryListResponse

    @POST("admin/categories")
    suspend fun createCategory(
        @Body request: CategoryRequest
    ): SingleCategoryResponse

    @PUT("admin/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Body request: CategoryRequest
    ): SingleCategoryResponse

    @DELETE("admin/categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: Int
    ): BasicResponse

    @GET("admin/activity-logs")
    suspend fun getActivityLogs(
        @Query("limit") limit: Int = 100
    ): ActivityLogListResponse
}