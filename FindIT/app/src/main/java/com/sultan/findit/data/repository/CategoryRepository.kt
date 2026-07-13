package com.sultan.findit.data.repository

import com.sultan.findit.data.model.Category
import com.sultan.findit.data.model.CategoryRequest
import com.sultan.findit.data.remote.ApiService

class CategoryRepository(
    private val apiService: ApiService
) {

    suspend fun getPublicCategories(): Result<List<Category>> {
        return try {
            val response = apiService.getCategories()
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminCategories(): Result<List<Category>> {
        return try {
            val response = apiService.getAdminCategories()
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCategory(name: String): Result<String> {
        return try {
            val response = apiService.createCategory(CategoryRequest(name))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCategory(id: Int, name: String): Result<String> {
        return try {
            val response = apiService.updateCategory(id, CategoryRequest(name))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(id: Int): Result<String> {
        return try {
            val response = apiService.deleteCategory(id)
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}