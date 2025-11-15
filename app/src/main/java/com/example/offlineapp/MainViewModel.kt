package com.example.offlineapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        val apiService = ApiService.create()
        repository = UserRepository(database.userDao(), apiService)

        observeUsers()
        refreshUsers()
    }

    private fun observeUsers() {
        viewModelScope.launch {
            repository.getAllUsers().collect { userList ->
                _users.value = userList
            }
        }
    }

    fun refreshUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.refreshUsers()
                .onSuccess {
                    _errorMessage.value = "Data refreshed successfully"
                }
                .onFailure { error ->
                    _errorMessage.value = "Failed to fetch: ${error.message}. Showing cached data."
                }

            _isLoading.value = false
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                observeUsers()
            } else {
                repository.searchUsers(query).collect { userList ->
                    _users.value = userList
                }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}