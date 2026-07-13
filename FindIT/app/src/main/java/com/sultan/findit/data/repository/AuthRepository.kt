package com.sultan.findit.data.repository

import android.content.Context
import com.sultan.findit.data.model.LoginRequest
import com.sultan.findit.data.model.LoginResponse
import com.sultan.findit.data.model.ProfileResponse
import com.sultan.findit.data.model.RegisterRequest
import com.sultan.findit.data.remote.ApiService
import com.sultan.findit.datastore.TokenManager
import kotlinx.coroutines.runBlocking

class AuthRepository(
    private val apiService: ApiService,
    private val context: Context
) {

    private val tokenManager = TokenManager(context)

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(
                LoginRequest(email = email, password = password)
            )

            val token = response.token
            val user = response.user

            if (response.success && !token.isNullOrBlank() && user != null) {
                tokenManager.saveSession(token, user)

                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.register(
                RegisterRequest(name = name, email = email, password = password)
            )

            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun profile(): Result<ProfileResponse> {
        return try {
            Result.success(apiService.profile())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<String> {
        return try {
            val response = apiService.logout()
            tokenManager.clearSession()
            Result.success(response.message)
        } catch (e: Exception) {
            tokenManager.clearSession()
            Result.failure(e)
        }
    }

    fun clearLocalSession() {
        runBlocking {
            tokenManager.clearSession()
        }
    }
}