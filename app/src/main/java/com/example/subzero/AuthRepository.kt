package com.example.subzero.network

import com.google.gson.Gson

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
}

object AuthRepository {

    private val gson = Gson()

    // read instance fresh each call so test injection w/ ApiClient.setInstanceForTesting() works
    private val api get() = ApiClient.instance

    // login logic
    suspend fun login(email: String, password: String): AuthResult<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                AuthResult.Success(response.body()!!)
            } else {
                val error = gson.fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
                AuthResult.Error(error?.error ?: "Login failed")
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }

    // register logic
    suspend fun register(email: String, password: String): AuthResult<RegisterResponse> {
        return try {
            val response = api.register(RegisterRequest(email, password))
            if (response.isSuccessful) {
                AuthResult.Success(response.body()!!)
            } else {
                val error = gson.fromJson(response.errorBody()?.string(), ErrorResponse::class.java)
                AuthResult.Error(error?.error ?: "Registration failed")
            }
        } catch (e: Exception) {
            AuthResult.Error("Network error: ${e.localizedMessage}")
        }
    }
}