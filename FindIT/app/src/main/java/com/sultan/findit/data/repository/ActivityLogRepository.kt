package com.sultan.findit.data.repository

import com.sultan.findit.data.model.ActivityLogListResponse
import com.sultan.findit.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ActivityLogRepository(private val apiService: ApiService) {
    fun getActivityLogs(limit: Int = 100): Flow<Result<ActivityLogListResponse>> = flow {
        try {
            val response = apiService.getActivityLogs(limit)
            emit(Result.success(response))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}