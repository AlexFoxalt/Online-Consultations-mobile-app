package com.study.onlineconsultations.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.study.onlineconsultations.data.AuthRepository
import com.study.onlineconsultations.data.AuthResult
import com.study.onlineconsultations.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isRegisterMode: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val currentUser: User? = null,
    val message: String? = null,
    val isLoading: Boolean = false
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun setRegisterMode(isRegisterMode: Boolean) {
        _uiState.update { state ->
            state.copy(
                isRegisterMode = isRegisterMode,
                message = null
            )
        }
    }

    fun onFullNameChanged(value: String) {
        _uiState.update { it.copy(fullName = value, message = null) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, message = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, message = null) }
    }

    fun onConfirmPasswordChanged(value: String) {
        _uiState.update { it.copy(confirmPassword = value, message = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isRegisterMode) {
            register(state)
        } else {
            login(state)
        }
    }

    fun logout() {
        _uiState.update {
            it.copy(
                currentUser = null,
                password = "",
                confirmPassword = "",
                message = "Logged out successfully"
            )
        }
    }

    private fun register(state: AuthUiState) {
        val fullName = state.fullName.trim()
        val email = state.email.trim()
        val password = state.password
        val confirmPassword = state.confirmPassword

        if (fullName.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.update { it.copy(message = "Please fill all fields") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(message = "Password must be at least 6 characters") }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(message = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            when (val result = repository.register(fullName, email, password)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            currentUser = result.user,
                            isLoading = false,
                            message = "Welcome, ${result.user.fullName}"
                        )
                    }
                }

                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = result.message
                        )
                    }
                }
            }
        }
    }

    private fun login(state: AuthUiState) {
        val email = state.email.trim()
        val password = state.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(message = "Please fill all fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            when (val result = repository.login(email, password)) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            currentUser = result.user,
                            isLoading = false,
                            message = "Welcome back, ${result.user.fullName}"
                        )
                    }
                }

                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = result.message
                        )
                    }
                }
            }
        }
    }
}

class AuthViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
