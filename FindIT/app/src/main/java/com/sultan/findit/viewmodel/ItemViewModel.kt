package com.sultan.findit.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sultan.findit.data.local.database.AppDatabase
import com.sultan.findit.data.model.Category
import com.sultan.findit.data.model.StatusCounts
import com.sultan.findit.data.remote.RetrofitClient
import com.sultan.findit.data.repository.CategoryRepository
import com.sultan.findit.data.repository.ItemFormData
import com.sultan.findit.data.repository.ItemRepository
import com.sultan.findit.data.util.toFriendlyMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ItemViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.create(application.applicationContext)
    private val database = AppDatabase.getDatabase(application.applicationContext)

    private val itemRepository = ItemRepository(
        context = application.applicationContext,
        apiService = apiService,
        itemDao = database.itemDao()
    )

    private val categoryRepository = CategoryRepository(apiService)

    private var searchJob: Job? = null

    val localItems = itemRepository.observeLocalItems().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    var categories by mutableStateOf<List<Category>>(emptyList())
        private set

    var counts by mutableStateOf(StatusCounts())
        private set

    var selectedStatus by mutableStateOf("all")
        private set

    var searchQuery by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isSyncing by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadCategories()
    }

    fun onSearchChange(value: String, isAdmin: Boolean = false) {
        searchQuery = value
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            if (isAdmin) {
                loadAdminItems()
            } else {
                refreshUserItems()
            }
        }
    }

    fun onStatusChange(value: String) {
        selectedStatus = value
        loadAdminItems()
    }

    fun refreshUserItems() {
        viewModelScope.launch {
            isSyncing = true
            itemRepository.refreshPublishedItems(
                search = searchQuery.takeIf { it.isNotBlank() }
            ).onSuccess {
                errorMessage = null
            }.onFailure {
                errorMessage = it.toFriendlyMessage()
            }
            isSyncing = false
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getPublicCategories().onSuccess {
                categories = it
            }.onFailure {
                if (categories.isEmpty()) {
                    errorMessage = it.toFriendlyMessage()
                }
            }
        }
    }

    fun loadAdminItems() {
        viewModelScope.launch {
            isLoading = true
            syncAdminItems()
            isLoading = false
        }
    }

    fun createItem(form: ItemFormData, photoUri: Uri?, onSuccess: () -> Unit) {
        if (photoUri == null) {
            errorMessage = "Foto wajib diisi."
            return
        }

        viewModelScope.launch {
            isLoading = true
            itemRepository.createItem(form, photoUri).onSuccess {
                successMessage = it
                errorMessage = null
                syncAdminItems()
                onSuccess()
            }.onFailure {
                errorMessage = it.toFriendlyMessage()
            }
            isLoading = false
        }
    }

    fun updateItem(id: Int, form: ItemFormData, photoUri: Uri?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            itemRepository.updateItem(id, form, photoUri).onSuccess {
                successMessage = it
                errorMessage = null
                syncAdminItems()
                onSuccess()
            }.onFailure {
                errorMessage = it.toFriendlyMessage()
            }
            isLoading = false
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            isLoading = true
            itemRepository.deleteItem(id).onSuccess {
                successMessage = it
                errorMessage = null
                syncAdminItems()
            }.onFailure {
                errorMessage = it.toFriendlyMessage()
            }
            isLoading = false
        }
    }

    fun publishItem(id: Int) {
        viewModelScope.launch {
            isLoading = true
            itemRepository.publishItem(id).onSuccess {
                successMessage = it
                errorMessage = null
                syncAdminItems()
            }.onFailure {
                errorMessage = it.toFriendlyMessage()
            }
            isLoading = false
        }
    }

    fun closeItem(id: Int, photoUri: Uri?, onSuccess: () -> Unit) {
        if (photoUri == null) {
            errorMessage = "Foto bukti wajib diisi."
            return
        }

        viewModelScope.launch {
            isLoading = true
            itemRepository.closeItem(id, photoUri).onSuccess {
                successMessage = it
                errorMessage = null
                syncAdminItems()
                onSuccess()
            }.onFailure {
                errorMessage = it.toFriendlyMessage()
            }
            isLoading = false
        }
    }

    fun clearMessage() {
        errorMessage = null
        successMessage = null
    }

    private suspend fun syncAdminItems() {
        itemRepository.getAdminItems(
            status = selectedStatus,
            search = searchQuery
        ).onSuccess {
            counts = it
            errorMessage = null
        }.onFailure {
            counts = calculateLocalCounts()
            errorMessage = it.toFriendlyMessage()
        }
    }

    private suspend fun calculateLocalCounts(): StatusCounts {
        val items = itemRepository.observeLocalItems().first()

        return StatusCounts(
            draft = items.count { it.status.equals("draft", ignoreCase = true) },
            published = items.count { it.status.equals("published", ignoreCase = true) },
            closed = items.count { it.status.equals("closed", ignoreCase = true) }
        )
    }
}