package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SessionManager
import com.example.data.model.UserSession
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val email: String = "",
    val fullName: String = "",
    val isResetSent: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val authRepository = AuthRepository(
        sessionManager = sessionManager,
        articleDao = database.articleDao()
    )

    val sessionState: StateFlow<UserSession> = authRepository.sessionState

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun login(emailInput: String, passwordInput: String, onSuccess: () -> Unit) {
        if (emailInput.isBlank() || passwordInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter email and password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val response = authRepository.signIn(emailInput, passwordInput)
            if (response.success) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Logged in successfully!") }
                onSuccess()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = response.errorMessage ?: "Login failed.") }
            }
        }
    }

    fun signUp(
        fullNameInput: String,
        emailInput: String,
        passwordInput: String,
        confirmPasswordInput: String,
        onSuccess: () -> Unit
    ) {
        if (fullNameInput.isBlank() || emailInput.isBlank() || passwordInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please fill in all required fields.") }
            return
        }
        if (passwordInput != confirmPasswordInput) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }
        if (passwordInput.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val response = authRepository.signUp(emailInput, passwordInput, fullNameInput)
            if (response.success) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Account created successfully!") }
                onSuccess()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = response.errorMessage ?: "Signup failed.") }
            }
        }
    }

    fun forgotPassword(emailInput: String) {
        if (emailInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val response = authRepository.resetPassword(emailInput)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isResetSent = true,
                    successMessage = "Password reset instructions sent to $emailInput if registered."
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.update { AuthUiState() }
        }
    }

    fun updateProfileName(newName: String) {
        if (newName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name cannot be empty.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val success = authRepository.updateProfileName(newName.trim())
            if (success) {
                _uiState.update { it.copy(isLoading = false, successMessage = "Profile updated successfully!") }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to update profile.") }
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val success = authRepository.deleteAccount()
            _uiState.update { AuthUiState() }
            onSuccess()
        }
    }

    fun continueAsGuest() {
        authRepository.continueAsGuest()
    }

    fun savePreferences(newsLanguage: String, notificationsEnabled: Boolean) {
        viewModelScope.launch {
            authRepository.savePreferences(newsLanguage, notificationsEnabled)
        }
    }
}
