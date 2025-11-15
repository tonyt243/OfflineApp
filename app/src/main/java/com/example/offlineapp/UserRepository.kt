package com.example.offlineapp

import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao,
    private val apiService: ApiService
) {
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()

    fun searchUsers(query: String): Flow<List<User>> = userDao.searchUsers(query)

    suspend fun refreshUsers(): Result<Unit> {
        return try {
            val users = apiService.getUsers()
            userDao.insertUsers(users)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
