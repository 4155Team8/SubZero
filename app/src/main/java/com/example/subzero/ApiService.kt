package com.example.subzero.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// ── Request bodies ──────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String
)

// ── Response bodies ─────────────────────────────────────────────────────────

data class AuthUser(
    val id: Int,
    val email: String
)

data class LoginResponse(
    val message: String,
    val token: String,
    val user: AuthUser
)

data class RegisterResponse(
    val message: String,
    val user: AuthUser
)

data class ErrorResponse(
    val error: String
)

// ── Retrofit interface ───────────────────────────────────────────────────────

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>
}
