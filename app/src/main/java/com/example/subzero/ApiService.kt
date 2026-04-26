package com.example.subzero.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// endpoint bodies

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String)
data class newEmailRequest(val email: String?)
data class newEmailResponse(val message: String?, val user: updatedEmailUser)
data class updatedEmailUser(val email: String?)
data class newPasswordRequest(val password: String?)
data class newPasswordResponse(val message: String?, val user: updatedPasswordUser)
data class deleteAccResponse(val message: String?)
data class updatedPasswordUser(val password_hash: String?)
data class AuthUser(val id: Int, val email: String)
data class NameRequest(val name: String?)
data class NameResponse(val message: String?, val user: UpdatedUser)
data class UpdatedUser(val name: String?, val email: String)
data class LoginResponse(val message: String, val token: String, val user: AuthUser)
data class RegisterResponse(val message: String, val user: AuthUser)
data class ErrorResponse(val error: String)
data class ForgotPasswordRequest(val email: String)
data class MessageResponse(val message: String)

// Budget
data class BudgetRequest(val monthly_budget: Double)
data class BudgetResponse(val message: String, val monthly_budget: Double)

// Reminders toggle
data class RemindersRequest(val reminders_enabled: Boolean)
data class RemindersResponse(val message: String, val reminders_enabled: Boolean)

// Monthly spend history entry
data class MonthlySpendResponse(
    val year: Int,
    val month: Int,
    val month_label: String,
    val total_spend: Double
)

// Dashboard — wraps subscriptions + budget + monthly history
data class DashboardResponse(
    val subscriptions: List<SubscriptionResponse>,
    val monthly_budget: Double,
    val monthly_spend: List<MonthlySpendResponse>
)

// subscription get

data class SubscriptionResponse(
    val id: Int,
    val name: String,
    val cost: Double,
    val category: String,
    val billing_cycle: String,
    val renewal_date: String?,
    val created_at: String,
    val updated_at: String,
    val category_id: Int? = null,
    val billing_cycle_id: Int? = null
)

data class AlertResponse(
    val id: Int,
    val subscription_id: Int,
    val reminder_date: String,
    val name: String,
    val description: String,
    val sent_at: String?,
    val created_at: String
)
data class ProfileResponse(
    val id: Int,
    val email: String,
    val created_at: String,
    val name: String?,
    val reminders_enabled: Int,
    val monthly_budget: Double
)

data class CategoryResponse(
    val id: Int,
    val name: String,
    val is_custom: Int
)

data class BillingCycleResponse(
    val id: Int,
    val name: String,
    val is_custom: Int
)

// interface

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<MessageResponse>

    @POST("auth/new-email")
    suspend fun newEmail(@Header("Authorization") token: String, @Body body: newEmailRequest): Response<newEmailResponse>

    @POST("auth/new-password")
    suspend fun newPassword(@Header("Authorization") token: String, @Body body: newPasswordRequest): Response<newPasswordResponse>

    @POST("profile/name")
    suspend fun changeName(@Header("Authorization") token: String, @Body body: NameRequest): Response<NameResponse>

    @PUT("profile/budget")
    suspend fun updateBudget(@Header("Authorization") token: String, @Body body: BudgetRequest): Response<BudgetResponse>

    @PUT("profile/reminders")
    suspend fun updateReminders(@Header("Authorization") token: String, @Body body: RemindersRequest): Response<RemindersResponse>

    /** Returns DashboardResponse: subscriptions + monthly_budget + monthly_spend */
    @GET("subscriptions")
    suspend fun getDashboard(
        @Header("Authorization") token: String
    ): Response<DashboardResponse>

    @GET("reminders")
    suspend fun getReminders(
        @Header("Authorization") token: String
    ): Response<List<AlertResponse>>

    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>

    @DELETE("auth/delete-account")
    suspend fun deleteAccount(
        @Header("Authorization") token: String
    ): Response<deleteAccResponse>
    
    @POST("subscriptions")
    suspend fun createSubscription(
        @Header("Authorization") token: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<SubscriptionResponse>

    @PUT("subscriptions/{id}")
    suspend fun updateSubscription(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<SubscriptionResponse>

    @DELETE("subscriptions/{id}")
    suspend fun deleteSubscription(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<MessageResponse>

    @GET("subscriptions/categories")
    suspend fun getCategories(
        @Header("Authorization") token: String
    ): Response<List<CategoryResponse>>

    @GET("subscriptions/billing-cycles")
    suspend fun getBillingCycles(
        @Header("Authorization") token: String
    ): Response<List<BillingCycleResponse>>

    @DELETE("reminders/clear-all")
    suspend fun clearAllAlerts(
        @Header("Authorization") token: String
    ): Response<MessageResponse>
}
