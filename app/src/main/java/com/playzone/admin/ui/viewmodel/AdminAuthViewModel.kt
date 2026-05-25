package com.playzone.admin.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.playzone.admin.data.model.UserProfile
import com.playzone.admin.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AdminAuthViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(repository.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    val currentUserId: String get() = repository.currentUser?.uid ?: ""
    val isAdmin: Boolean get() = _userProfile.value?.role == "admin"

    init {
        if (repository.currentUser != null) {
            loadUserProfile()
            // FIX: Refresh FCM token setiap kali admin sudah login
            viewModelScope.launch { repository.saveAdminFcmToken() }
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            val profile = repository.getUserProfile(currentUserId)
            _userProfile.value = profile
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            repository.signIn(email.trim(), password)
                .onSuccess {
                    loadUserProfile()
                    // FIX: Simpan FCM token admin ke Firestore agar siap menerima notifikasi
                    repository.saveAdminFcmToken()
                    _isLoggedIn.value = true
                    _uiState.value = AuthUiState(successMessage = "Login berhasil")
                }
                .onFailure {
                    _uiState.value = AuthUiState(error = it.message ?: "Login gagal")
                }
        }
    }

    fun logout() {
        repository.signOut()
        _isLoggedIn.value = false
        _userProfile.value = null
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
