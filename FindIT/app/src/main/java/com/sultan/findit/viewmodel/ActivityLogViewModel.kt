package com.sultan.findit.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sultan.findit.data.model.ActivityLog
import com.sultan.findit.data.repository.ActivityLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ActivityLogViewModel(private val repository: ActivityLogRepository) : ViewModel() {
    private val _logs = MutableStateFlow<List<ActivityLog>>(emptyList())
    val logs: StateFlow<List<ActivityLog>> = _logs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchLogs(limit: Int = 100) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository.getActivityLogs(limit).collect { result ->
                result.onSuccess { response ->
                    if (response.success) {
                        _logs.value = response.data
                    } else {
                        _error.value = response.message
                    }
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Terjadi kesalahan yang tidak diketahui"
                }
                _isLoading.value = false
            }
        }
    }
}