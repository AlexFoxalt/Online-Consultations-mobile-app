package com.study.onlineconsultations.data

import android.database.sqlite.SQLiteConstraintException

class AuthRepository(private val userDao: UserDao) {

    suspend fun register(fullName: String, email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        val newUser = User(
            fullName = fullName.trim(),
            email = normalizedEmail,
            password = password
        )

        return try {
            val userId = userDao.insert(newUser)
            if (userId > 0) {
                AuthResult.Success(newUser.copy(id = userId.toInt()))
            } else {
                AuthResult.Error("Could not create user")
            }
        } catch (_: SQLiteConstraintException) {
            // Email has a unique index, so this is the expected duplicate-user path.
            AuthResult.Error("User with this email already exists")
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        val user = userDao.getByEmailAndPassword(normalizedEmail, password)
        return if (user != null) {
            AuthResult.Success(user)
        } else {
            AuthResult.Error("Wrong email or password")
        }
    }
}

sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data class Error(val message: String) : AuthResult
}
