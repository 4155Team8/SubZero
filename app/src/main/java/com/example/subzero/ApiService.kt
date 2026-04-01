package com.example.subzero.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

// endpoint bodies

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String)
data class AuthUser(val id: Int, val email: String)
data class LoginResponse(val message: String, val token: String, val user: AuthUser)
data class RegisterResponse(val message: String, val user: AuthUser)
data class ErrorResponse(val error: String)
data class ForgotPasswordRequest(val email: String)
data class MessageResponse(val message: String)

// subscription get

data class SubscriptionResponse(
    val id: Int,
    val name: String,
    val cost: Double,
    val category: String,
    val billing_cycle: String,
    val created_at: String,
    val updated_at: String
)

// interface

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<MessageResponse>

    @GET("subscriptions")
    suspend fun getSubscriptions(
        @Header("Authorization") token: String
    ): Response<List<SubscriptionResponse>>
}