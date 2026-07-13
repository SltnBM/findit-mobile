package com.sultan.findit.datastore

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.sultan.findit.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context.applicationContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context.applicationContext,
        "findit_session_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _tokenFlow = MutableStateFlow(prefs.getString("token", null))
    val tokenFlow: StateFlow<String?> = _tokenFlow.asStateFlow()

    private val _roleFlow = MutableStateFlow(prefs.getString("role", null))
    val roleFlow: StateFlow<String?> = _roleFlow.asStateFlow()

    private val _nameFlow = MutableStateFlow(prefs.getString("name", null))
    val nameFlow: StateFlow<String?> = _nameFlow.asStateFlow()

    private val _emailFlow = MutableStateFlow(prefs.getString("email", null))
    val emailFlow: StateFlow<String?> = _emailFlow.asStateFlow()

    fun getApiKey(): String {
        val storedKey = prefs.getString("api_key", null)
        if (storedKey.isNullOrEmpty()) {
            prefs.edit().putString("api_key", "findit_api_key_secret_123").apply()
            return "findit_api_key_secret_123"
        }
        return storedKey
    }

    fun saveTokenExp(exp: String) {
        prefs.edit().putString("token_exp", exp).apply()
    }

    fun isTokenExpired(): Boolean {
        val expStr = prefs.getString("token_exp", null) ?: return false
        return try {
            val expTime = LocalDateTime.parse(expStr.replace(" ", "T"))
            val now = LocalDateTime.now()
            now.isAfter(expTime)
        } catch (e: Exception) {
            true
        }
    }

    fun isLoggedIn(): Boolean {
        val token = prefs.getString("token", "") ?: ""
        return token.isNotEmpty() && !isTokenExpired()
    }

    suspend fun saveSession(token: String, user: User, tokenExp: String? = null) {
        prefs.edit().apply {
            putString("token", token)
            putString("role", user.role)
            putInt("user_id", user.id)
            putString("name", user.name)
            putString("email", user.email)
            if (tokenExp != null) {
                putString("token_exp", tokenExp)
            }
        }.apply()

        _tokenFlow.value = token
        _roleFlow.value = user.role
        _nameFlow.value = user.name
        _emailFlow.value = user.email
    }

    suspend fun clearSession() {
        prefs.edit().clear().apply()

        _tokenFlow.value = null
        _roleFlow.value = null
        _nameFlow.value = null
        _emailFlow.value = null
    }

    suspend fun getToken(): String? {
        return prefs.getString("token", null)
    }

    suspend fun getRole(): String? {
        return prefs.getString("role", null)
    }

    fun getUsername(): String {
        return prefs.getString("name", "User") ?: "User"
    }
}