package com.flashmob.platonus.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashmob.platonus.data.model.User
import com.flashmob.platonus.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.login(email, password)
            if (result.isSuccess) {
                _loginResult.value = LoginResult(success = result.getOrNull())
            } else {
                _loginResult.value = LoginResult(error = result.exceptionOrNull()?.message ?: "Ошибка входа")
            }
            _isLoading.value = false
        }
    }
}

data class LoginResult (
    val success: User? = null,
    val error: String? = null
)