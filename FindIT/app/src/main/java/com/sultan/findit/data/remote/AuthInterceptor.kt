package com.sultan.findit.data.remote

import android.content.Context
import com.sultan.findit.datastore.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val tokenManager = TokenManager(context)
        val originalRequest = chain.request()

        val token = runBlocking { tokenManager.getToken() } ?: ""
        val apiKey = tokenManager.getApiKey()

        val newRequest = originalRequest.newBuilder()
            .addHeader("Accept", "application/json")
            .apply {
                if (apiKey.isNotEmpty()) {
                    addHeader("X-API-Key", apiKey)
                }
                if (token.isNotEmpty()) {
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .build()

        val response = chain.proceed(newRequest)

        if (response.code == 401) {
            runBlocking {
                tokenManager.clearSession()
            }
        }

        return response
    }
}