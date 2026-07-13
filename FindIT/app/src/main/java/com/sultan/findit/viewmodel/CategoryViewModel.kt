package com.sultan.findit.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sultan.findit.data.model.Category
import com.sultan.findit.data.remote.RetrofitClient
import com.sultan.findit.data.repository.CategoryRepository
import kotlinx.coroutines.launch
import com.sultan.findit.data.util.toFriendlyMessage

class CategoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.create(application.applicationContext)
    private val repository = CategoryRepository(apiService)

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set

    var nameInput by mutableStateOf("")
        private set

    var editingCategory by mutableStateOf<Category?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        set

    var successMessage by mutableStateOf<String?>(null)
        private set

    fun onNameChange(value: String) {
        nameInput = value
    }

    fun setEditing(category: Category) {
        editingCategory = category
        nameInput = category.name
    }

    fun clearEditing() {
        editingCategory = null
        nameInput = ""
    }

    fun loadCategories() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = repository.getAdminCategories()

            result.onSuccess { data ->
                categories = data
            }.onFailure { error ->
                errorMessage = error.toFriendlyMessage()
            }

            isLoading = false
        }
    }

    fun saveCategory() {
        if (nameInput.isBlank()) {
            errorMessage = "Nama kategori wajib diisi."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            val currentEditing = editingCategory

            val result = if (currentEditing == null) {
                repository.createCategory(nameInput)
            } else {
                repository.updateCategory(currentEditing.id, nameInput)
            }

            result.onSuccess { message ->
                successMessage = message
                clearEditing()
                loadCategories()
            }.onFailure { error ->
                errorMessage = error.toFriendlyMessage()
            }

            isLoading = false
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            val result = repository.deleteCategory(id)

            result.onSuccess { message ->
                successMessage = message
                loadCategories()
            }.onFailure { error ->
                errorMessage = error.toFriendlyMessage()
            }

            isLoading = false
        }
    }

    fun clearMessage() {
        errorMessage = null
        successMessage = null
    }
}