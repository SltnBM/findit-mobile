package com.sultan.findit.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.sultan.findit.data.local.dao.ItemDao
import com.sultan.findit.data.local.entity.ItemEntity
import com.sultan.findit.data.model.Item
import com.sultan.findit.data.model.StatusCounts
import com.sultan.findit.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ItemRepository(
    private val context: Context,
    private val apiService: ApiService,
    private val itemDao: ItemDao
) {

    fun observeLocalItems(): Flow<List<ItemEntity>> = itemDao.observeAllItems()

    fun observeLocalItemById(id: Int): Flow<ItemEntity?> = itemDao.observeItemById(id)

    suspend fun refreshPublishedItems(
        search: String? = null,
        categoryId: Int? = null,
        location: String? = null,
        date: String? = null
    ): Result<Unit> {
        return try {
            val response = apiService.getItems(
                search = search?.takeIf { it.isNotBlank() },
                categoryId = categoryId,
                location = location?.takeIf { it.isNotBlank() },
                date = date?.takeIf { it.isNotBlank() }
            )

            val entities = response.data.map { it.toEntity() }
            itemDao.deleteAllItems()
            itemDao.upsertItems(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminItems(status: String? = null, search: String? = null): Result<StatusCounts> {
        return try {
            val response = apiService.getAdminItems(
                status = status?.takeIf { it.isNotBlank() && it != "all" },
                search = search?.takeIf { it.isNotBlank() }
            )

            val entities = response.data.map { it.toEntity() }
            itemDao.deleteAllItems()
            itemDao.upsertItems(entities)
            Result.success(response.counts ?: StatusCounts())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createItem(form: ItemFormData, photoUri: Uri): Result<String> {
        return try {
            val response = apiService.createItem(
                form.categoryId.toString().asTextPart(),
                form.name.asTextPart(),
                form.description.asTextPart(),
                createPhotoPart(photoUri),
                form.foundLocation.asTextPart(),
                form.foundDate.asTextPart(),
                form.pickupLocation.asTextPart(),
                form.characteristics.asTextPart(),
                form.adminNote.asTextPart()
            )
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateItem(id: Int, form: ItemFormData, photoUri: Uri?): Result<String> {
        return try {
            val response = apiService.updateItem(
                id,
                form.categoryId.toString().asTextPart(),
                form.name.asTextPart(),
                form.description.asTextPart(),
                photoUri?.let { createPhotoPart(it) },
                form.foundLocation.asTextPart(),
                form.foundDate.asTextPart(),
                form.pickupLocation.asTextPart(),
                form.characteristics.asTextPart(),
                form.adminNote.asTextPart()
            )
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteItem(id: Int): Result<String> {
        return try {
            Result.success(apiService.deleteItem(id).message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publishItem(id: Int): Result<String> {
        return try {
            Result.success(apiService.publishItem(id).message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun closeItem(id: Int, claimPhotoUri: Uri): Result<String> {
        return try {
            Result.success(apiService.closeItem(id, createClaimPhotoPart(claimPhotoUri)).message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Item.toEntity() = ItemEntity(
        id, categoryId, category?.name, name, description, photoUrl, claimPhotoUrl,
        foundLocation, foundDate, pickupLocation, characteristics, adminNote, status
    )

    private fun String.asTextPart() = toRequestBody("text/plain".toMediaType())

    private fun createPhotoPart(uri: Uri): MultipartBody.Part {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: throw Exception()
        val body = bytes.toRequestBody(context.contentResolver.getType(uri)?.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("photo", context.contentResolver.getFileName(uri), body)
    }

    private fun createClaimPhotoPart(uri: Uri): MultipartBody.Part {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: throw Exception()
        val body = bytes.toRequestBody(context.contentResolver.getType(uri)?.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("claim_photo", context.contentResolver.getFileName(uri), body)
    }

    private fun android.content.ContentResolver.getFileName(uri: Uri): String {
        var name = "file_${System.currentTimeMillis()}.jpg"
        query(uri, null, null, null, null)?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = it.getString(index)
            }
        }
        return name
    }
}

data class ItemFormData(
    val categoryId: Int, val name: String, val description: String,
    val foundLocation: String, val foundDate: String, val pickupLocation: String,
    val characteristics: String, val adminNote: String
)