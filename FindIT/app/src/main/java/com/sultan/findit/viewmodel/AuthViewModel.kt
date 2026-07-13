package com.sultan.findit.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sultan.findit.data.remote.RetrofitClient
import com.sultan.findit.data.repository.AuthRepository
import com.sultan.findit.datastore.TokenManager
import kotlinx.coroutines.launch
import com.sultan.findit.data.util.toFriendlyMessage

class AuthViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val apiService = RetrofitClient.create(context)
    private val repository = AuthRepository(apiService, context)
    private val tokenManager = TokenManager(context)

    var name by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var emailError by mutableStateOf<String?>(null)
        private set

    var passwordError by mutableStateOf<String?>(null)
        private set

    var nameError by mutableStateOf<String?>(null)
        private set

    var role by mutableStateOf<String?>(null)
        private set

    var token by mutableStateOf<String?>(null)
        private set

    var isCheckingAuth by mutableStateOf(true)
        private set

    init {
        viewModelScope.launch {
            val savedToken = tokenManager.getToken()
            if (!savedToken.isNullOrEmpty() && !tokenManager.isTokenExpired()) {
                token = savedToken
                role = tokenManager.getRole()
            } else {
                tokenManager.clearSession()
            }
            isCheckingAuth = false
        }
    }

    fun onNameChange(value: String) {
        name = value
        if (nameError != null && value.isNotBlank()) {
            nameError = null
        }
    }

    fun onEmailChange(value: String) {
        email = value
        if (emailError != null && value.isNotBlank()) {
            emailError = null
        }
        if (errorMessage != null) errorMessage = null
    }

    fun onPasswordChange(value: String) {
        password = value
        if (passwordError != null && value.isNotBlank()) {
            passwordError = null
        }
        if (errorMessage != null) errorMessage = null
    }

    fun login(onSuccess: (role: String) -> Unit) {
        emailError = null
        passwordError = null
        errorMessage = null

        var hasError = false

        if (email.isBlank()) {
            emailError = "Alamat email wajib diisi"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Format email tidak valid"
            hasError = true
        }

        if (password.isBlank()) {
            passwordError = "Kata sandi wajib diisi"
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            isLoading = true

            val result = repository.login(email, password)

            result.onSuccess { response ->
                val userRole = response.user?.role.orEmpty()
                val userToken = response.token
                val userObj = response.user

                if (!userToken.isNullOrBlank() && userObj != null) {
                    tokenManager.saveSession(userToken, userObj)
                }

                token = userToken
                role = userRole
                onSuccess(userRole)
            }.onFailure { error ->
                errorMessage = error.toFriendlyMessage()
            }

            isLoading = false
        }
    }

    fun register(onSuccess: (role: String) -> Unit) {
        nameError = null
        emailError = null
        passwordError = null
        errorMessage = null

        var hasError = false

        if (name.isBlank()) {
            nameError = "Nama lengkap wajib diisi"
            hasError = true
        }

        if (email.isBlank()) {
            emailError = "Alamat email wajib diisi"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Format email tidak valid"
            hasError = true
        }

        if (password.isBlank()) {
            passwordError = "Kata sandi wajib diisi"
            hasError = true
        } else if (password.length < 6) {
            passwordError = "Kata sandi minimal berisi 6 karakter"
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            isLoading = true

            val result = repository.register(name, email, password)

            result.onSuccess { response ->
                val userRole = response.user?.role.orEmpty()
                val userToken = response.token
                val userObj = response.user

                if (!userToken.isNullOrBlank() && userObj != null) {
                    tokenManager.saveSession(userToken, userObj)
                }

                token = userToken
                role = userRole

                name = ""
                email = ""
                password = ""

                onSuccess(userRole)
            }.onFailure { error ->
                errorMessage = error.toFriendlyMessage()
            }

            isLoading = false
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            isLoading = true

            repository.logout()
            token = null
            role = null
            name = ""
            email = ""
            password = ""
            emailError = null
            passwordError = null
            nameError = null

            isLoading = false
            onDone()
        }
    }

    fun clearError() {
        errorMessage = null
        emailError = null
        passwordError = null
        nameError = null
    }
}