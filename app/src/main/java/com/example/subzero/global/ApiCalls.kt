package com.example.subzero.global

import android.content.Context
import android.se.omapi.Session
import android.util.Log
import android.widget.Toast
import com.example.subzero.SessionManager
import com.example.subzero.network.*
import com.example.subzero.network.ApiClient

class ApiCalls {
    var util = Utility()

    data class fullResponse(
        val email: String?,
        val name: String?,
        val remindersEnabled: Boolean?,
        val createdAt: String?,
        val subscriptions: List<SubscriptionResponse>?,
        val numSubs: Int?,
        val reminders: List<AlertResponse>?,
        val monthlyBudget: Double,
        val monthlySpend: List<MonthlySpendResponse>
    )

    suspend fun loadFull(cont: Context): fullResponse? {
        val token = SessionManager.getToken(cont) ?: return null

        return try {
            val profileResponse = ApiClient.instance.getProfile("Bearer $token")
            if (!profileResponse.isSuccessful) return null

            val body = profileResponse.body()
            val email = body?.email ?: "Not available"
            val name = body?.name ?: "John Doe"
            val remindersEnabled = body?.reminders_enabled == 1
            val memberSince = util.formatToMonthYear(body?.created_at)
            val monthlyBudget = body?.monthly_budget ?: 0.0

            val dashResponse = ApiClient.instance.getDashboard("Bearer $token")
            val subscriptions: List<SubscriptionResponse> =
                if (dashResponse.isSuccessful) dashResponse.body()?.subscriptions ?: emptyList() else emptyList()
            val monthlySpend: List<MonthlySpendResponse> =
                if (dashResponse.isSuccessful) dashResponse.body()?.monthly_spend ?: emptyList() else emptyList()
            val numSubs = subscriptions.size

            val remResponse = ApiClient.instance.getReminders("Bearer $token")
            val reminders: List<AlertResponse> =
                if (remResponse.isSuccessful) remResponse.body() ?: emptyList() else emptyList()

            Log.d("ApiCalls", "Loaded $numSubs subs, budget=$monthlyBudget")

            fullResponse(email, name, remindersEnabled, memberSince, subscriptions, numSubs, reminders, monthlyBudget, monthlySpend)

        } catch (e: Exception) {
            Log.e("PROFILE_ERROR", e.localizedMessage ?: "", e)
            Toast.makeText(cont, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    suspend fun loadDashboard(cont: Context): DashboardResponse? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val response = ApiClient.instance.getDashboard("Bearer $token")
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("ApiCalls", "loadDashboard error: ${e.localizedMessage}", e)
            Toast.makeText(cont, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    suspend fun loadProfile(cont: Context): ProfileResponse? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val profResponse = ApiClient.instance.getProfile("Bearer $token")
            profResponse.body()
        } catch (e: Exception) {
            Toast.makeText(cont, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    suspend fun loadReminders(cont: Context): List<AlertResponse>? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val alertResponse = ApiClient.instance.getReminders("Bearer $token")
            alertResponse.body()
        } catch (e: Exception) {
            Toast.makeText(cont, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    suspend fun updateName(cont: Context, name: String?): NameResponse? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val nameResponse = ApiClient.instance.changeName("Bearer $token", NameRequest(name))
            if (nameResponse.isSuccessful) nameResponse.body() else null
        } catch (e: Exception) {
            Log.e("Error", e.localizedMessage ?: "", e)
            null
        }
    }

    suspend fun updateNotifications(cont: Context, enabled: Boolean): RemindersResponse? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val response = ApiClient.instance.updateReminders("Bearer $token", RemindersRequest(enabled))
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("ApiCalls", "updateNotifications error: ${e.localizedMessage}", e)
            null
        }
    }

    suspend fun updateBudget(cont: Context, budget: Double): BudgetResponse? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val response = ApiClient.instance.updateBudget("Bearer $token", BudgetRequest(budget))
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("ApiCalls", "updateBudget error: ${e.localizedMessage}", e)
            null
        }
    }

    suspend fun updateEmail(cont: Context, email: String?): newEmailResponse? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val emailResponse = ApiClient.instance.newEmail("Bearer $token", newEmailRequest(email))
            if (emailResponse.isSuccessful) emailResponse.body() else null
        } catch (e: Exception) {
            Log.e("Error", e.localizedMessage ?: "", e)
            null
        }
    }

    suspend fun updatePassword(cont: Context, password: String?): newPasswordResponse? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val passResponse = ApiClient.instance.newPassword("Bearer $token", newPasswordRequest(password))
            if (passResponse.isSuccessful) passResponse.body() else null
        } catch (e: Exception) {
            Log.e("Error", e.localizedMessage ?: "", e)
            null
        }
    }

    suspend fun deleteAccount(cont: Context): deleteAccResponse? {
        val token = SessionManager.getToken((cont)) ?: return null
        return try {
            val delRes = ApiClient.instance.deleteAccount("Bearer $token")
            if (delRes.isSuccessful) delRes.body() else null
        } catch (e: Exception) {
            Log.e("Error", e.localizedMessage ?: "", e)
            null
        }
    }

    suspend fun loadSubscriptions(cont: Context): List<SubscriptionResponse>? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val response = ApiClient.instance.getDashboard("Bearer $token")
            if (response.isSuccessful) response.body()?.subscriptions else null
        } catch (e: Exception) {
            Log.e("ApiCalls", "loadSubscriptions error: ${e.localizedMessage}", e)
            Toast.makeText(cont, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }
    
    suspend fun createSubscription(
        cont: Context,
        name: String,
        cost: Double,
        categoryId: Int,
        billingCycleId: Int,
        renewalDate: String
    ): SubscriptionResponse? {
        return try {
            val token = SessionManager.getToken(cont) ?: return null
            val body = mapOf(
                "name" to name,
                "cost" to cost,
                "category_id" to categoryId,
                "billing_cycle_id" to billingCycleId,
                "renewal_date" to renewalDate
            )
            val response = ApiClient.instance.createSubscription("Bearer $token", body)
            return if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("ApiCalls", "createSubscription error: $e")
            null
        }
    }

    suspend fun updateSubscription(
        cont: Context,
        id: Int,
        name: String,
        cost: Double,
        categoryId: Int,
        billingCycleId: Int,
        renewalDate: String
    ): SubscriptionResponse? {
        return try {
            val token = SessionManager.getToken(cont) ?: return null
            val body = mapOf(
                "name" to name,
                "cost" to cost,
                "category_id" to categoryId,
                "billing_cycle_id" to billingCycleId,
                "renewal_date" to renewalDate
            )
            val response = ApiClient.instance.updateSubscription("Bearer $token", id, body)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("ApiCalls", "updateSubscription error: $e")
            null
        }
    }

    suspend fun deleteSubscription(cont: Context, id: Int): Boolean {
        return try {
            val token = SessionManager.getToken(cont) ?: return false
            val response = ApiClient.instance.deleteSubscription("Bearer $token", id)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ApiCalls", "deleteSubscription error: $e")
            false
        }
    }

    suspend fun loadCategories(cont: Context): List<CategoryResponse>? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val response = ApiClient.instance.getCategories("Bearer $token")
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("ApiCalls", "loadCategories error: $e")
            null
        }
    }

    suspend fun loadBillingCycles(cont: Context): List<BillingCycleResponse>? {
        val token = SessionManager.getToken(cont) ?: return null
        return try {
            val response = ApiClient.instance.getBillingCycles("Bearer $token")
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("ApiCalls", "loadBillingCycles error: $e")
            null
        }
    }

    suspend fun clearReminders(cont: Context): Boolean {
        val token = SessionManager.getToken(cont) ?: return false
        return try {
            val response = ApiClient.instance.clearAllAlerts("Bearer $token")
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("ApiCalls", "clearReminders error: ${e.localizedMessage}", e)
            false
        }
    }
}
